package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
        @SerialName("addressFirstLine")
        val addressFirstLine: String? = null,

        @SerialName("city")
        val city: String? = null,

        @SerialName("postCode")
        val postCode: String? = null,

        @SerialName("stateCode")
        val stateCode: String? = null,

        @SerialName("countryName")
        val countryName: String? = null,
)