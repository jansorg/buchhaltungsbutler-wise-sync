package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType(val wiseId: String) {
    @SerialName("CARD")
    Card("CARD"),

    @SerialName("CONVERSION")
    Conversion("CONVERSION"),

    @SerialName("DEPOSIT")
    Deposit("DEPOSIT"),

    @SerialName("TRANSFER")
    Transfer("TRANSFER"),

    @SerialName("MONEY_ADDED")
    MoneyAdded("MONEY_ADDED"),

    @SerialName("UNKNOWN")
    Unknown("UNKNOWN"),
}