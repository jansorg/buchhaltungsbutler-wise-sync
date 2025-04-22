package dev.ja.wise.model

import dev.ja.model.Currency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Amount(
    @SerialName("currency")
    val currency: Currency,

    @SerialName("value")
    val value: AmountValue,

    @SerialName("zero")
    val zero: Boolean? = null,
) {
    override fun toString(): String {
        return String.format("%.2f %s", value, currency)
    }

    val isZero: Boolean = zero ?: (value == 0.0)

    val isNonZero: Boolean = !isZero
}