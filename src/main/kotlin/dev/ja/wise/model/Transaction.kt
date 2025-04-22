package dev.ja.wise.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Transaction(
    val type: CreditType,
    val date: Instant,
    val amount: Amount,
    val referenceNumber: TransactionReferenceNumber,
    val details: TransactionDetails,
    val isWiseCashback: Boolean,
) {
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