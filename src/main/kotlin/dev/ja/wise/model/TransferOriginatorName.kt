package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferOriginatorName(
        @SerialName("givenName")
        val givenName: String,
        @SerialName("middleName")
        val middleName: String,
        @SerialName("familyName")
        val familyName: String,
        @SerialName("patronymicName")
        val patronymicName: String,
        @SerialName("fullName")
        val fullName: String,
        @SerialName("dateOfBirth")
        val dateOfBirth: String,
        @SerialName("businessRegistrationCode")
        val businessRegistrationCode: String,
)