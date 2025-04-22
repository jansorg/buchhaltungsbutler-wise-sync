package dev.ja.sync.model

import dev.ja.bhb.model.AccountId
import dev.ja.bhb.model.Transaction
import dev.ja.bhb.requests.Account
import dev.ja.wise.model.TransactionReferenceNumber

data class CollectedBhbData(
    val accounts: List<Account>,
    val transactions: Map<AccountId, List<Transaction>>,
    val syncedWiseTransactions: Set<TransactionReferenceNumber>,
) {
    val isEmpty: Boolean
        get() {
            return transactions.isEmpty() && syncedWiseTransactions.isEmpty()
        }

    val accountsById: Map<AccountId, Account>
        get() {
            return accounts.filter { it.accountNumber != null }.associateBy { it.accountNumber!! }
        }
}

