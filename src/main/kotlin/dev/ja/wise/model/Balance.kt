package dev.ja.wise.model

import dev.ja.model.Currency
import dev.ja.wise.json.WiseInstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Balance(
        @SerialName("id")
        val id: BalanceId,
        @SerialName("currency")
        val currency: Currency,
        @SerialName("type")
        val type: BalanceType,
        @SerialName("amount")
        val amount: Amount,
        @SerialName("creationTime")
        @Serializable(WiseInstantSerializer::class)
        val creationTime: Instant,
        @SerialName("modificationTime")
        @Serializable(WiseInstantSerializer::class)
        val modificationTime: Instant,
        @SerialName("visible")
        val visible: Boolean,
)