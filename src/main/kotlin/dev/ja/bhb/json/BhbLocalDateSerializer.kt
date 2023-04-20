package dev.ja.bhb.json

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

object BhbLocalDateSerializer : KSerializer<LocalDate> {
    private val converter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .parseStrict()
            .toFormatter()

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BhbLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return java.time.LocalDate.parse(decoder.decodeString(), converter).toKotlinLocalDate()
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toJavaLocalDate().format(converter))
    }
}