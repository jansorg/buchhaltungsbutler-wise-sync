package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDetails(
        @SerialName("type")
        val type: TransactionType,

        @SerialName("description")
        val description: String,

        @SerialName("amount")
        val amount: Amount? = null,

        @SerialName("sourceAmount")
        val sourceAmount: Amount? = null,

        @SerialName("targetAmount")
        val targetAmount: Amount? = null,

        @SerialName("fee")
        val conversionFee: Amount? = null,

        @SerialName("rate")
        val rate: Double? = null,

        @SerialName("senderName")
        val senderName: String? = null,

        @SerialName("senderAccount")
        val senderAccount: String? = null,

        @SerialName("recipient")
        val recipient: TransactionRecipient? = null,

        @SerialName("paymentReference")
        val paymentReference: String? = null,

        @SerialName("category")
        val category: String? = null,

        @SerialName("merchant")
        val merchant: Merchant? = null,
)