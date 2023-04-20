package dev.ja.bhb

import dev.ja.bhb.model.Transaction
import dev.ja.bhb.model.TransactionId
import dev.ja.bhb.requests.*

/**
 * Client to the Buchhaltungsbutler API.
 */
interface BhbClient : AutoCloseable {
    suspend fun getAccounts(): AccountsResponse

    suspend fun getTransactions(request: GetTransactions): List<Transaction>

    suspend fun getTransaction(id: TransactionId): Transaction
    suspend fun addTransaction(transaction: AddTransaction): TransactionId
    suspend fun addBatchTransactions(transactions: List<AddTransaction>): List<TransactionId>

    suspend fun addPosting(posting: AddTransactionPosting)

    /**
     * Adds a comment to a transaction or receipt,
     * use [dev.ja.bhb.requests.AddReceiptComment] or [dev.ja.bhb.requests.AddTransactionComment].
     */
    suspend fun addComment(comment: AddComment)
}