package com.eferraz.filestore.brokeragenote.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object BrazilianLocalDateSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BrazilianLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        val raw = decoder.decodeString()
        val parts = raw.split('/')
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid Brazilian date format: $raw")
        }

        val day = parts[0].toIntOrNull()
            ?: throw IllegalArgumentException("Invalid day in Brazilian date: $raw")
        val month = parts[1].toIntOrNull()
            ?: throw IllegalArgumentException("Invalid month in Brazilian date: $raw")
        val year = parts[2].toIntOrNull()
            ?: throw IllegalArgumentException("Invalid year in Brazilian date: $raw")

        return LocalDate(year, month, day)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val day = value.dayOfMonth.toString().padStart(2, '0')
        val month = value.monthNumber.toString().padStart(2, '0')
        encoder.encodeString("$day/$month/${value.year}")
    }
}
