package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeDetails(
        @SerialName("fromAmount")
        val fromAmount: Amount? = null,

        @SerialName("toAmount")
        val toAmount: Amount? = null,

        @SerialName("rate")
        val rate: Double,
)