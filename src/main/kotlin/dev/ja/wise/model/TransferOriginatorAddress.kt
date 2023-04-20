package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferOriginatorAddress(
        @SerialName("firstLine")
        val firstLine: String,
        @SerialName("city")
        val city: String,
        @SerialName("stateCode")
        val stateCode: String,
        @SerialName("countryCode")
        val countryCode: String,
        @SerialName("postCode")
        val postCode: String,
)