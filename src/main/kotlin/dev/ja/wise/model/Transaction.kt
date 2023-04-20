package dev.ja.wise.model

import dev.ja.wise.json.WiseInstantSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
        @SerialName("type")
        val type: CreditType,

        @SerialName("date")
        @Serializable(WiseInstantSerializer::class)
        val date: Instant,

        @SerialName("amount")
        val amount: Amount,

        @SerialName("totalFees")
        val totalFees: Amount,

        @SerialName("runningBalance")
        val runningBalance: Amount,

        @SerialName("referenceNumber")
        val referenceNumber: TransactionReferenceNumber,

        @SerialName("details")
        val details: TransactionDetails,

        @SerialName("exchangeDetails")
        val exchangeDetails: ExchangeDetails? = null,
) {
    val isWiseCashback: Boolean
        get() {
            return details.type == TransactionType.Unknown && referenceNumber.isBalanceCashback()
        }

    val toShortString: String
        get() {
            val more = listOfNotNull(
                    details.senderName ?: details.recipient?.name,
                    details.paymentReference?.trim()?.takeUnless(String::isEmpty),
                    referenceNumber
            ).joinToString(" | ", prefix = " -- ")
            return "[${date.toLocalDateTime(TimeZone.currentSystemDefault()).date}] $amount $more"
        }
}