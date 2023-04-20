package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountIssuer(
        @SerialName("name")
        val name: String,

        @SerialName("firstLine")
        val firstLine: String,

        @SerialName("city")
        val city: String? = null,

        @SerialName("postCode")
        val postCode: String? = null,

        @SerialName("stateCode")
        val stateCode: String? = null,

        @SerialName("country")
        val country: String? = null,
)