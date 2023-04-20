package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountHolder(
        @SerialName("type")
        val type: AccountHolderType,

        @SerialName("firstName")
        val firstName: String? = null,

        @SerialName("lastName")
        val lastName: String? = null,

        @SerialName("address")
        val address: Address? = null,
)