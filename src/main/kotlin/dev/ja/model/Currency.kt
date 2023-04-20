package dev.ja.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Currency, shared between Buchhaltungsbutler and Wise, because both are using the standard currency codes.
 */
@Serializable
enum class Currency(val id: String) {
    @SerialName("EUR")
    EUR("EUR"),

    @SerialName("USD")
    USD("USD");

    companion object {
        fun of(code: String): Currency {
            return Currency.valueOf(code)
        }
    }
}