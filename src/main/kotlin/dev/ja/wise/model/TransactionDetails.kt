package dev.ja.wise.model

import dev.ja.model.Currency

data class TransactionDetails(
    val type: TransactionType,
    val amount: Amount? = null,
    // only defined for money conversions
    val sourceCurrency: Currency? = null,
    // only defined for money conversions
    val targetCurrency: Currency? = null,
    val senderName: String? = null,
    val recipient: TransactionRecipient? = null,
    val paymentReference: String? = null,
)