package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Merchant(
        @SerialName("name")
        val name: String? = null,
        @SerialName("firstLine")
        val firstLine: String? = null,
        @SerialName("postCode")
        val postCode: String? = null,
        @SerialName("city")
        val city: String? = null,
        @SerialName("stateCode")
        val state: String? = null,
        @SerialName("country")
        val country: String? = null,
        @SerialName("category")
        val category: String? = null,
)