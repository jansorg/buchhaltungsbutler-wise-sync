package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
        @SerialName("id")
        val id: ProfileId,
        @SerialName("type")
        val type: ProfileType,
)