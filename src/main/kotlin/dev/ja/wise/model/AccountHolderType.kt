package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccountHolderType(val wiseId: String) {
    @SerialName("PERSONAL")
    Personal("PERSONAL"),

    @SerialName("BUSINESS")
    Business("BUSINESS"),
}