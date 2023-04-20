package dev.ja.bhb

import dev.ja.bhb.model.TransactionId
import dev.ja.bhb.requests.AddComment
import dev.ja.bhb.requests.AddTransaction
import dev.ja.bhb.requests.AddTransactionPosting

/**
 * Read-Only client, which returns empty results for modifying requests and delegates read-only requests to the
 * [BhbClient] passed to the constructor.
 */
class ReadOnlyBhbClient(private val delegate: BhbClient) : BhbClient by delegate {
    override suspend fun addTransaction(transaction: AddTransaction): TransactionId {
        // read-only
        return ""
    }

    override suspend fun addBatchTransactions(transactions: List<AddTransaction>): List<TransactionId> {
        // read-only
        return emptyList()
    }

    override suspend fun addPosting(posting: AddTransactionPosting) {
        // read-only
    }

    override suspend fun addComment(comment: AddComment) {
        // read-only
    }
}