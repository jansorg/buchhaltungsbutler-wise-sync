package dev.ja.bhb.model

import dev.ja.bhb.json.BhbLocalDateTimeSerializer
import dev.ja.bhb.model.AccountId
import dev.ja.bhb.model.Amount
import dev.ja.model.Currency
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
        @SerialName("id_by_customer")
        val id: Int,
        @SerialName("account")
        val account: AccountId,
        @SerialName("to_from")
        val toFrom: String,
        @Serializable(BhbLocalDateTimeSerializer::class)
        @SerialName("booking_date")
        val bookingDate: LocalDateTime,
        @Serializable(BhbLocalDateTimeSerializer::class)
        @SerialName("value_date")
        val valueDate: LocalDateTime,
        @SerialName("amount")
        val amount: Amount,
        @SerialName("currency")
        val currency: Currency,
        @SerialName("account_number")
        val accountNumber: String? = null,
        @SerialName("bank_code")
        val bankCode: String? = null,
        @SerialName("bank_name")
        val bankName: String? = null,
        @SerialName("purpose")
        val purpose: String? = null,
        @SerialName("type")
        val type: String? = null,
        @SerialName("booking_text")
        val bookingText: String? = null,
        @SerialName("payment_reference")
        val paymentReference: String? = null,
)