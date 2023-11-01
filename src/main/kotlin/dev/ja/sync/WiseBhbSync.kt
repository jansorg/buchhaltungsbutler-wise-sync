package dev.ja.sync

import dev.ja.bhb.BhbClient
import dev.ja.bhb.KtorBhbClient
import dev.ja.bhb.ReadOnlyBhbClient
import dev.ja.bhb.model.AccountId
import dev.ja.bhb.model.Vat
import dev.ja.bhb.requests.*
import dev.ja.model.Currency
import dev.ja.sync.model.CollectedBhbData
import dev.ja.sync.model.CollectedWiseData
import dev.ja.sync.model.SyncConfig
import dev.ja.wise.KtorWiseClient
import dev.ja.wise.WiseClient
import dev.ja.wise.model.*
import kotlinx.datetime.*
import java.time.YearMonth
import kotlin.math.absoluteValue

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

        val wiseData = collectWiseData(wiseClient, firstDay, lastDay)
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
        println("   Syncing ${notSynced.size} transaction from Wise.com to BHB...")

        notSynced.sortedBy { it.date }.forEach { wiseTransaction ->
            println("         ${wiseTransaction.toShortString}")
            val syncFeeTransaction = wiseTransaction.needsFeeSync()
            when {
                // incoming transfer
                wiseTransaction.details.type == TransactionType.Deposit -> {
                    syncWiseDeposit(bhbAccountId, wiseTransaction, bhbClient, syncFeeTransaction)
                }

                // outgoing transfer
                wiseTransaction.details.type == TransactionType.Transfer -> {
                    syncWiseTransfer(bhbAccountId, wiseTransaction, bhbClient, syncFeeTransaction)
                }

                // transfer between multi-currency accounts (i.e. currency exchange), e.g. USD to EUR
                wiseTransaction.details.type == TransactionType.Conversion -> {
                    syncWiseConversion(bhbAccountId, wiseTransaction, bhbClient, syncFeeTransaction)
                }

                // credit of Wise cashback
                wiseTransaction.isWiseCashback -> {
                    syncWiseBalanceCashback(bhbAccountId, wiseTransaction, bhbClient)
                }

                else -> throw IllegalStateException("Skipping unsupported transaction type ")
            }
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

        // not every Wise transaction has a fee, but every one with a fee must have been synced already
        if (bhbData.isNotEmpty) {
            val wiseTransactionsWithFee = wiseData.statements.values.asSequence().flatMap { it.transactions }
                .filter { it.needsFeeSync() }
                .map { it.referenceNumber }
                .toSet()

            if (!bhbData.syncedWiseTransactions.containsAll(bhbData.syncedWiseTransactionFees)) {
                throw IllegalStateException("BuchhaltungsButler is out-of-sync: missing transaction for fee transactions")
            }

            // all synced transactions, which have a fee, must have been synced (no partial sync is allowed)
            val syncedTransactionsWithFees = wiseTransactionsWithFee.filter { it in bhbData.syncedWiseTransactions }
            if (!bhbData.syncedWiseTransactionFees.containsAll(syncedTransactionsWithFees)) {
                throw IllegalStateException("BuchhaltungsButler is out-of-sync: found missing fee transactions")
            }
        }

        wiseData.statements.values.forEach {
            it.transactions.forEach(::validateWiseTransaction)
        }
    }

    private fun validateWiseTransaction(transaction: Transaction) {
        if (transaction.totalFees.isNonZero && transaction.totalFees.currency != transaction.amount.currency) {
            throw IllegalStateException("amount and fee must have the same currency: $transaction")
        }

        if (transaction.totalFees.value < 0) {
            throw IllegalStateException("negative transaction fee is unsupported: $transaction")
        }

        if (transaction.details.type == TransactionType.Conversion) {
            transaction.details.sourceAmount?.currency
                ?: throw IllegalStateException("missing source amount currency: $transaction")
            transaction.details.targetAmount?.currency
                ?: throw IllegalStateException("missing target amount currency: $transaction")
        }
    }

    private suspend fun collectWiseData(
        wiseClient: WiseClient,
        intervalStart: LocalDate,
        intervalEnd: LocalDate
    ): CollectedWiseData {
        println("Fetching data from Wise.com...")

        val profile = wiseClient.getProfiles().firstOrNull { it.type == ProfileType.Business }
            ?: throw IllegalStateException("Unable to locate Wise Business profile")

        // from "startDate 00:00:00" to "next day after endDate 00:00:00" to include all of the last day
        val intervalStartTime = intervalStart.atStartOfDayIn(TimeZone.UTC)
        val intervalEndTime = intervalEnd.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC)

        val balances = wiseClient.getBalances(profile.id)
        val currencyToBalance = balances.associateBy { it.currency }
        val statements = currencyToBalance.mapValues { (_, balance) ->
            wiseClient.getBalanceStatement(profile.id, balance, intervalStartTime, intervalEndTime)
        }

        return CollectedWiseData(profile, currencyToBalance.keys, currencyToBalance, statements)
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

        val syncedTransactionFees = idToTransactions.values.asSequence().flatten().mapNotNullTo(mutableSetOf()) {
            it.bookingText?.let { ref -> bhbWiseFeeIdMatcher.find(ref)?.value?.removePrefix(wiseFeeIdPrefix) }
        }

        return CollectedBhbData(accounts, idToTransactions, syncedTransactions, syncedTransactionFees)
    }

    /**
     * Syncs an incoming Wise transfer.
     * Fees may have been applied for the transfer.
     * It creates an item for the total amount and optionally another item for the fee.
     */
    private suspend fun syncWiseDeposit(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
        bhbClient: BhbClient,
        syncFeeTransaction: Boolean
    ) {
        if (wiseTransaction.amount.value < 0) {
            throw IllegalStateException("Negative deposit amount are unsupported")
        }

        val transactions = mutableListOf(
            AddTransaction(
                bhbAccountId,
                wiseTransaction.details.senderName ?: syncConfig.unknownSender,
                // amount excludes the fee, amount+fee was sent by the sender
                wiseTransaction.amount.value + wiseTransaction.totalFees.value,
                wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
                Currency.of(wiseTransaction.amount.currency.id),
                bookingText = bookingTextWithSyncId(wiseTransaction),
                // not adding account number, because it may not be an IBAN, e.g. for USD accounts
                //accountNumber = wiseTransaction.details.recipient?.bankAccount,
            )
        )

        if (syncFeeTransaction) {
            transactions += createWiseFeeTransactionItem(bhbAccountId, wiseTransaction, WiseFeeType.TransferOrDeposit)
        }

        bhbClient.addBatchTransactions(transactions).also { transactionIds ->
            if (syncFeeTransaction && syncConfig.bhbFeePostingId != null) {
                updateFeePosting(transactionIds.getOrNull(1), bhbClient, syncConfig.bhbFeePostingId)
            }
        }
    }

    /**
     * Syncs an outgoing Wise transfer.
     * Fees may have been applied for the transfer.
     * It creates an item for the total amount and optionally another item for the fee.
     */
    private suspend fun syncWiseTransfer(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
        bhbClient: BhbClient,
        syncFeeTransaction: Boolean
    ) {
        if (wiseTransaction.type == CreditType.Credit) {
            throw IllegalStateException("CREDIT transfers are not supported")
        }

        val transactions = mutableListOf(
            AddTransaction(
                bhbAccountId,
                wiseTransaction.details.recipient?.name ?: wiseTransaction.details.senderName ?: "unknown",
                // always negative amount, fee is always positive
                wiseTransaction.amount.value + wiseTransaction.totalFees.value,
                wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
                Currency.of(wiseTransaction.amount.currency.id),
                bookingText = bookingTextWithSyncId(wiseTransaction),
                accountNumber = wiseTransaction.details.recipient?.bankAccount,
            )
        )

        if (syncFeeTransaction) {
            transactions += createWiseFeeTransactionItem(bhbAccountId, wiseTransaction, WiseFeeType.TransferOrDeposit)
        }

        bhbClient.addBatchTransactions(transactions).also { transactionIds ->
            if (syncFeeTransaction && syncConfig.bhbFeePostingId != null) {
                updateFeePosting(transactionIds.getOrNull(1), bhbClient, syncConfig.bhbFeePostingId)
            }
        }
    }

    private suspend fun syncWiseConversion(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
        bhbClient: BhbClient,
        syncFeeTransaction: Boolean
    ) {
        // these were validated
        val sourceCurrency = wiseTransaction.details.sourceAmount?.currency!!
        val targetCurrency = wiseTransaction.details.targetAmount?.currency!!

        val transactions = mutableListOf(
            AddTransaction(
                bhbAccountId,
                syncConfig.wiseConversionLabel(sourceCurrency, targetCurrency),
                // conversion of the total amount (credited value + conversion fee)
                wiseTransaction.amount.value + wiseTransaction.totalFees.value,
                wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
                Currency.of(wiseTransaction.amount.currency.id),
                bookingText = bookingTextWithSyncId(wiseTransaction),
                accountNumber = wiseTransaction.details.recipient?.bankAccount,
            )
        )

        if (syncFeeTransaction) {
            transactions += createWiseFeeTransactionItem(bhbAccountId, wiseTransaction, WiseFeeType.Conversion)
        }

        bhbClient.addBatchTransactions(transactions).also { transactionIds ->
            if (syncFeeTransaction && syncConfig.bhbFeePostingId != null) {
                updateFeePosting(transactionIds.getOrNull(1), bhbClient, syncConfig.bhbFeePostingId)
            }
        }
    }

    private suspend fun updateFeePosting(id: TransferIdString?, bhbClient: BhbClient, postingAccountId: AccountId) {
        // fixme
        if (true || syncConfig.readOnly == true) {
            //println("         skipping fee posting")
            return
        }

        if (id == null) {
            throw IllegalStateException("Invalid fee transaction id for Wise transaction")
        }

        // first, query the EUR amount of the new BHB transaction
        val bhbTransaction = bhbClient.getTransaction(id)

        println("   Updating fee posting...")
        val posting = PostingItem(postingAccountId, "posting text", Vat.None, bhbTransaction.amount.absoluteValue)
        bhbClient.addPosting(AddTransactionPosting(id, listOf(posting)))
    }

    private suspend fun syncWiseBalanceCashback(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
        bhbClient: BhbClient
    ) {
        bhbClient.addTransaction(
            AddTransaction(
                bhbAccountId,
                syncConfig.wiseSenderLabel,
                wiseTransaction.amount.value,
                wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
                Currency.of(wiseTransaction.amount.currency.id),
                bookingText = bookingTextWithSyncId(wiseTransaction, syncConfig.wiseCashbackLabel)
            )
        )
    }

    private fun createWiseFeeTransactionItem(
        bhbAccountId: AccountId,
        wiseTransaction: Transaction,
        wiseFeeType: WiseFeeType
    ): AddTransaction {
        return AddTransaction(
            bhbAccountId,
            syncConfig.wiseSenderLabel,
            -wiseTransaction.totalFees.value,
            wiseTransaction.date.toLocalDateTime(bhbLocalTimeZone),
            Currency.of(wiseTransaction.totalFees.currency.id),
            bookingText = feeTextWithSyncId(wiseTransaction, wiseFeeType)
        )
    }

    private fun bookingTextWithSyncId(
        transaction: Transaction,
        text: String? = transaction.details.paymentReference
    ): String {
        return listOfNotNull(text, "$wiseIdPrefix${transaction.referenceNumber}").joinToString(",\n")
    }

    private fun feeTextWithSyncId(transaction: Transaction, type: WiseFeeType): String {
        return listOfNotNull(
            type.label(syncConfig),
            "$wiseFeeIdPrefix${transaction.referenceNumber}"
        ).joinToString(",\n")
    }

    private fun createBhbClient(): BhbClient {
        val client = KtorBhbClient(syncConfig.bhbApiClient, syncConfig.bhbApiSecret, syncConfig.bhbApiKey)
        return when (syncConfig.readOnly) {
            true -> ReadOnlyBhbClient(client)
            else -> client
        }
    }

    private fun Transaction.needsFeeSync(): Boolean {
        return totalFees.isNonZero
    }

    enum class WiseFeeType {
        Conversion,
        TransferOrDeposit;

        fun label(syncConfig: SyncConfig): String {
            return when (this) {
                Conversion -> syncConfig.wiseFeeLabelConversion
                TransferOrDeposit -> syncConfig.wiseFeeLabel
            }
        }
    }
}