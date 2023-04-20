package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferOriginator(
        @SerialName("legalEntityType")
        val legalEntityType: String,
        @SerialName("reference")
        val reference: String,
        @SerialName("name")
        val name: TransferOriginatorName,
        @SerialName("address")
        val address: TransferOriginatorAddress
)