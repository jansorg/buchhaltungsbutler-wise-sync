package dev.ja.bhb.requests

import kotlinx.serialization.SerialName

sealed interface AddComment {
    val commentText: String
}

data class AddTransactionComment(
        @SerialName("comment_text")
        override val commentText: String,

        @SerialName("transaction_id_by_customer")
        val transactionId: String,
) : AddComment

data class AddReceiptComment(
        @SerialName("comment_text")
        override val commentText: String,

        @SerialName("receipt_id_by_customer")
        val receiptId: String,
) : AddComment