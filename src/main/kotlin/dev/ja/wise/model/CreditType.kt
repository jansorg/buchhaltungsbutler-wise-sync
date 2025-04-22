package dev.ja.wise.model

import jakarta.xml.bind.annotation.XmlEnum
import jakarta.xml.bind.annotation.XmlEnumValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@XmlEnum
enum class CreditType {
    @SerialName("DEBIT")
    @XmlEnumValue("DBIT")
    Debit,

    @SerialName("CREDIT")
    @XmlEnumValue("CRDT")
    Credit,
}