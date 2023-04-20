package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BalanceType(val wiseId: String) {
    @SerialName("STANDARD")
    Standard("STANDARD"),

    @SerialName("SAVINGS")
    Savings("SAVINGS")
}