package dev.ja.sync

import dev.ja.bhb.BhbClient
import dev.ja.bhb.KtorBhbClient
import dev.ja.bhb.ReadOnlyBhbClient
import dev.ja.bhb.model.AccountId
import dev.ja.bhb.requests.Account
import dev.ja.bhb.requests.AddTransaction
import dev.ja.bhb.requests.GetTransactions
import dev.ja.model.Currency
import dev.ja.sync.model.CollectedBhbData
import dev.ja.sync.model.CollectedWiseData
import dev.ja.sync.model.SyncConfig
import dev.ja.wise.KtorWiseClient
import dev.ja.wise.WiseClient
import dev.ja.wise.camt053.Camt053Parser
import dev.ja.wise.model.*
import kotlinx.datetime.*
import java.nio.file.Files
import java.time.YearMonth
import kotlin.io.path.Path

/**
 * Synchronize Wise transactions to your BHB account.
 */
class WiseBhbSync(private val syncConfig: SyncConfig) {
    // fixme unclear, which timezone is used by BHB
    private val bhbLocalTimeZone = TimeZone.of("Europe/Berlin")
    private val wiseIdPrefix = "#wise_"
    private val wiseFeeIdPrefix = "#wisefee_"
    private val bhbWiseIdMatcher = Regex("$wiseIdPrefix[a-zA-Z0-9_-]+")
    private val bhbWiseFeeIdMatcher = Regex("$wiseFeeIdPrefix[a-zA-Z0-9_-]+")

    suspend fun sync(fromMonth: YearMonth, toMonth: YearMonth) {
        val wiseClient = KtorWiseClient(syncConfig.wiseApiKey, syncConfig.wisePrivateKey)
        val bhbClient = createBhbClient()

        val firstDay = fromMonth.atDay(1).toKotlinLocalDate()
        val lastDay = toMonth.atEndOfMonth().toKotlinLocalDate()

        val readOnlyLabel = if (syncConfig.readOnly == true) " (Read-Only Mode)" else ""
        println("Syncing Wise to Buchhaltungsbutler$readOnlyLabel: $firstDay - $lastDay...")

        val wiseData = collectWiseData(wiseClient, firstDay, lastDay, syncConfig.wiseStatementsPaths)
        val bhbData = collectBhbData(bhbClient, firstDay, lastDay, syncConfig.bhbAccountToWiseCurrency.keys.toList())

        validate(wiseData, bhbData)

        syncConfig.bhbAccountToWiseCurrency.forEach { (bhbAccountId, currency) ->
            println("\nSyncing for currency $currency...")
            syncWiseToBhb(currency, wiseData, bhbData, bhbAccountId, bhbClient)
        }
    }

    private suspend fun syncWiseToBhb(
        currency: Currency,
        wiseData: CollectedWiseData,
        bhbData: CollectedBhbData,
        bhbAccountId: AccountId,
        bhbClient: BhbClient
    ) {
        val statement = wiseData.statements[currency]
            ?: throw IllegalStateException("missing statement for $currency")

        val notSynced = statement.transactions.filterNot { it.referenceNumber in bhbData.syncedWiseTransactions }
        println("   Syncing ${notSynced.size} transaction(s) from Wise.com to BHB...")

        val transactions = notSynced.sortedBy { it.date }.map { wiseTransaction ->
            println("         ${wiseTransaction.toShortString}")
            when {
                // credit of Wise cashback
                wiseTransaction.isWiseCashback -> {
                    createWiseBalanceCashbackTransaction(bhbAccountId, wiseTransaction)
                }

                // incoming transfer
                wiseTransaction.details.type == TransactionType.Deposit -> {
                    createWiseDepositTransaction(bhbAccountId, wiseTransaction)
                }

                // outgoing transfer
                wiseTransaction.details.type == TransactionType.Transfer -> {
                    createWiseTransferTransaction(bhbAccountId, wiseTransaction)
                }

                // transfer between multi-currency accounts (i.e. currency exchange), e.g. USD to EUR
                wiseTransaction.details.type == TransactionType.Conversion -> {
                    createWiseConversionTransaction(bhbAccountId, wiseTransaction)
                }

                else -> throw IllegalStateException("Skipping unsupported transaction type ")
            }
        }

        bhbClient.addBatchTransactions(transactions)

        val synced = statement.transactions.filter { it.referenceNumber in bhbData.syncedWiseTransactions }
        if (synced.isNotEmpty()) {
            println("   Skipping ${synced.size} already synced transaction(s):")
            synced.forEach { println("         ${it.toShortString}") }
        }
    }

    private fun validate(wiseData: CollectedWiseData, bhbData: CollectedBhbData) {
        syncConfig.bhbAccountToWiseCurrency.forEach { (bhbAccountId, currency) ->
            // verify that all Wise accounts are available
            if (currency !in wiseData.currencies) {
                throw IllegalStateException("Missing Wise account for BHB $bhbAccountId -> Wise currency $currency")
            }

            // verify that all BHB account are available
            if (bhbAccountId !in bhbData.accountsById) {
                throw IllegalStateException("BHB account $bhbAccountId does not exist. Please verify your configuration.")
            }
        }

        wiseData.statements.values.forEach {
            it.transactions.forEach(::validateWiseTransaction)
        }
    }

    private fun validateWiseTransaction(transaction: Transaction) {
        if (transaction.details.type == TransactionType.Conversion) {
            transaction.details.sourceCurrency
                ?: throw IllegalStateException("missing source amount currency: $transaction")
            transaction.details.targetCurrency
                ?: throw IllegalStateException("missing target amount currency: $transaction")
        }
    }

    private suspend fun collectWiseData(
        wiseClient: WiseClient,
        intervalStart: LocalDate,
        intervalEnd: LocalDate,
        wiseStatementsPaths: String
    ): CollectedWiseData {
        println("Fetching data from Wise.com...")

        val profile = wiseClient.getProfiles().firstOrNull { it.type == ProfileType.Business }
            ?: throw IllegalStateException("Unable to locate Wise Business profile")

        // from "startDate 00:00:00" to "next day after endDate 00:00:00" to include all of the last day
        val intervalStartTime = intervalStart.atStartOfDayIn(TimeZone.UTC)
        val intervalEndTime = intervalEnd.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC)

        // read CAMT.053 statements from the configured folder
        val camt053Folder = Path(wiseStatementsPaths)
        val camt053Statements = Files.list(camt053Folder)
            .filter { it.fileName.toString().endsWith(".xml") }
            .map { Camt053Parser().parse(Files.newInputStream(it))!!.bankToCustomerStatement!! }
            .toList()
        val currencyTransactions = camt053Statements
            .groupBy { it.statement?.account?.currency!! }
            .map { (currency, statements) ->
                val transactions = statements.flatMap { it.statement?.statementEntries ?: emptyList() }
                    .sortedBy { it.bookingDate!!.value }
                    .filter {
                        val date = it.bookingDate!!.value!!
                        date in intervalStartTime..intervalEndTime
                    }
                    .map { entry ->
                        val amount = entry.amount!!.toWiseAmount()
                        val isWiseCashback = entry.additionalInformation == "Balance cashback"

                        val transactionType = when {
                            entry.amountDetails?.transactionAmount?.currencyExchange != null -> TransactionType.Conversion
                            entry.creditDebitIndicator == CreditType.Credit -> TransactionType.Deposit
                            entry.creditDebitIndicator == CreditType.Debit -> TransactionType.Transfer
                            else -> throw IllegalStateException("Unsupported transaction type: $entry")
                        }

                        val referenceNumber = when {
                            isWiseCashback -> entry.bankTransactionCode?.proprietary?.code?.let { uuid ->
                                assert(uuid.length == 32)
                                // The old API returned
                                // 53450e1c5b504166344f6dd99fdd9ce0 as 53450e1c-5b50-4166-344f-6dd99fdd9ce0,
                                // and we have to pass it like that to sync with old items.
                                val fixedUUID = uuid.substring(0, 8) +
                                        "-" + uuid.substring(8, 12) +
                                        "-" + uuid.substring(12, 16) +
                                        "-" + uuid.substring(16, 20) +
                                        "-" + uuid.substring(20)
                                "BALANCE_CASHBACK-$fixedUUID"
                            }

                            else -> entry.bankTransactionCode?.proprietary?.code
                        } ?: throw java.lang.IllegalStateException("missing transaction code in $entry")

                        val senderPattern = Regex("^Received money from (.+?) with reference")
                        val senderName: String? = entry.additionalInformation?.let {
                            senderPattern.find(it)?.groupValues?.getOrNull(1)
                        }

                        val recipient = when {
                            entry.creditorName != null -> {
                                TransactionRecipient(entry.creditorName!!, null)
                            }

                            entry.additionalInformation?.startsWith("Wise Charges for:") == true -> {
                                TransactionRecipient(syncConfig.wiseSenderLabel)
                            }

                            else -> null
                        }

                        val details = TransactionDetails(
                            transactionType,
                            entry.amount.toWiseAmount(),
                            entry.amountDetails?.transactionAmount?.currencyExchange?.sourceCurrency,
                            entry.amountDetails?.transactionAmount?.currencyExchange?.targetCurrency,
                            senderName,
                            recipient,
                            entry.entityReference?.trim(),
                        )

                        Transaction(
                            entry.creditDebitIndicator!!,
                            entry.bookingDate?.value!!,
                            amount,
                            referenceNumber,
                            details,
                            isWiseCashback,
                        )
                    }

                currency to FlatBalanceStatement(transactions)
            }.toMap()

        return CollectedWiseData(profile, currencyTransactions.keys, currencyTransactions)
    }

    private suspend fun collectBhbData(
        bhbClient: BhbClient,
        firstDay: LocalDate,
        lastDay: LocalDate,
        accountIds: List<AccountId>
    ): CollectedBhbData {
        println("Fetching data from Buchhaltungsbutler...")
        val accounts = bhbClient.getAccounts()
        val existingAccountIds = accounts.mapNotNull(Account::accountNumber)

        // only fetch transactions of existing accounts, validation of the configured accounts happens later
        val idToTransactions = accountIds.filter { it in existingAccountIds }.associateWith { id ->
            bhbClient.getTransactions(GetTransactions(accountId = id, firstDate = firstDay, lastDate = lastDay))
        }

        val syncedTransactions = idToTransactions.values.asSequence().flatten().mapNotNullTo(mutableSetOf()) {
            it.bookingText?.let { ref -> bhbWiseIdMatcher.find(ref)?.value?.removePrefix(wiseIdPrefix) }
        }

        // Wise CAMT statements use ID "FEE-$id" for fees of transaction $id
        val syncedTransactionFees = idToTransactions.values.asSequence().flatten().mapNotNullTo(mutableSetOf()) {
            it.bookingText
                ?.let { ref -> bhbWiseFeeIdMatcher.find(ref)?.value?.removePrefix(wiseFeeIdPrefix) }
                ?.let { "FEE-$it" }
        }

        return CollectedBhbData(accounts, idToTransactions, syncedTransactions + syncedTransactionFees)
    }

    /**
     * Syncs an incoming Wise transfer.
     * Fees may have been applied for the transfer.
     * It creates an item for the total amount and optionally another item for the fee.
     */
    private fun createWiseDepositTransaction(bhbAccountId: AccountId, wiseTransaction: Transaction): AddTransaction {
        if (wiseTransaction.amount.value < 0) {
            throw IllegalStateException("Negative deposit amount are unsupported")
        }

        return AddTransaction(
            bhbAccountId,
            wiseTransaction.details.senderName ?: syncConfig.unknownSender,
            wiseTransaction.bhbAmount(),
            wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
            Currency.of(wiseTransaction.amount.currency.id),
            bookingText = bookingTextWithSyncId(wiseTransaction),
            // not adding account number, because it may not be an IBAN, e.g. for USD accounts
            //accountNumber = wiseTransaction.details.recipient?.bankAccount,
        )
    }

    /**
     * Syncs an outgoing Wise transfer.
     * Fees may have been applied for the transfer.
     * It creates an item for the total amount and optionally another item for the fee.
     */
    private fun createWiseTransferTransaction(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction
    ): AddTransaction {
        if (wiseTransaction.type == CreditType.Credit) {
            throw IllegalStateException("CREDIT transfers are not supported")
        }

        return AddTransaction(
            bhbAccountId,
            wiseTransaction.details.recipient?.name ?: wiseTransaction.details.senderName ?: "unknown",
            wiseTransaction.bhbAmount(),
            wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
            Currency.of(wiseTransaction.amount.currency.id),
            bookingText = bookingTextWithSyncId(wiseTransaction),
            accountNumber = wiseTransaction.details.recipient?.bankAccount,
        )
    }

    private fun createWiseConversionTransaction(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
    ): AddTransaction {
        val details = wiseTransaction.details
        return AddTransaction(
            bhbAccountId,
            syncConfig.wiseConversionLabel(details.sourceCurrency!!, details.targetCurrency!!),
            wiseTransaction.bhbAmount(),
            wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
            Currency.of(wiseTransaction.amount.currency.id),
            bookingText = bookingTextWithSyncId(wiseTransaction),
            accountNumber = details.recipient?.bankAccount,
        )
    }

    private fun createWiseBalanceCashbackTransaction(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction
    ): AddTransaction {
        return AddTransaction(
            bhbAccountId,
            syncConfig.wiseSenderLabel,
            wiseTransaction.amount.value,
            wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
            Currency.of(wiseTransaction.amount.currency.id),
            bookingText = bookingTextWithSyncId(wiseTransaction, syncConfig.wiseCashbackLabel)
        )
    }

    private fun bookingTextWithSyncId(
        transaction: Transaction,
        text: String? = transaction.details.paymentReference
    ): String {
        return listOfNotNull(text, "$wiseIdPrefix${transaction.referenceNumber}").joinToString(",\n")
    }

    private fun createBhbClient(): BhbClient {
        val client = KtorBhbClient(syncConfig.bhbApiClient, syncConfig.bhbApiSecret, syncConfig.bhbApiKey)
        return when (syncConfig.readOnly) {
            true -> ReadOnlyBhbClient(client)
            else -> client
        }
    }

    fun Transaction.bhbAmount(): dev.ja.bhb.model.Amount {
        return when (this.type) {
            CreditType.Debit -> -this.amount.value
            CreditType.Credit -> this.amount.value
        }
    }
}