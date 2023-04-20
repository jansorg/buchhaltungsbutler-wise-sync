package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRecipient(
        @SerialName("name")
        val name: String,
        @SerialName("bankAccount")
        val bankAccount: String? = null,
)