package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProfileType {
    @SerialName("BUSINESS")
    Business,

    @SerialName("PERSONAL")
    Personal,
}