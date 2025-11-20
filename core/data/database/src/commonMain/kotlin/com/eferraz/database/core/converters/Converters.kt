package com.eferraz.database.core.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * Type converters para Room Database.
 * Converte tipos do domínio para tipos compatíveis com SQLite.
 */
internal class Converters {

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
}

