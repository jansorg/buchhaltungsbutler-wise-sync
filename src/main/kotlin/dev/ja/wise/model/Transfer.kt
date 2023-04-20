package dev.ja.wise.model

import dev.ja.model.Currency
import dev.ja.wise.json.WiseLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transfer(
        @SerialName("id")
        val id: TransferId,
        @SerialName("user")
        val user: Int,
        @SerialName("targetAccount")
        val targetAccount: Int,
        @SerialName("sourceAccount")
        val sourceAccount: Int? = null,
        @SerialName("quote")
        val quoteIdV1: Int? = null,
        @SerialName("quoteUuid")
        val quoteUuid: String? = null,
        @SerialName("status")
        val status: String,
        @SerialName("rate")
        val rate: Double,
        @Serializable(WiseLocalDateTimeSerializer::class)
        @SerialName("created")
        val created: LocalDateTime,
        @SerialName("business")
        val business: Int? = null,
        @SerialName("details")
        val details: TransferDetails,
        @SerialName("hasActiveIssues")
        val hasActiveIssues: Boolean,
        @SerialName("sourceCurrency")
        val sourceCurrency: Currency,
        @SerialName("sourceValue")
        val sourceValue: AmountValue,
        @SerialName("targetCurrency")
        val targetCurrency: Currency,
        @SerialName("targetValue")
        val targetValue: AmountValue,
        @SerialName("customerTransactionId")
        val customerTransactionId: String,
        @SerialName("originator")
        val originator: TransferOriginator? = null,
)