package dev.ja.bhb.requests

import dev.ja.bhb.json.BhbLocalDateSerializer
import dev.ja.bhb.model.AccountId
import dev.ja.bhb.model.TransactionId
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTransactions(
        @SerialName("id_by_customer_from")
        val firstId: TransactionId? = null,
        @SerialName("id_by_customer_to")
        val lastId: TransactionId? = null,
        @Serializable(BhbLocalDateSerializer::class)
        @SerialName("date_from")
        val firstDate: LocalDate? = null,
        @SerialName("date_to")
        val lastDate: LocalDate? = null,
        @SerialName("account")
        val accountId: AccountId? = null,
        // payer or payee
        @SerialName("to_from")
        val toFrom: String? = null,
        @SerialName("limit")
        val limit: Int? = null,
        @SerialName("offset")
        val offset: Int? = null,
)
