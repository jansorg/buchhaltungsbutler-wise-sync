package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreditType(val wiseId: String) {
    @SerialName("DEBIT")
    Debit("DEBIT"),

    @SerialName("CREDIT")
    Credit("CREDIT"),
}