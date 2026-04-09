package com.eferraz.database.core.converters

import androidx.room3.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Type converters para Room Database.
 * Converte tipos do domínio para tipos compatíveis com SQLite.
 */
internal class Converters {

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it) }

    // YearMonth converters
    @TypeConverter
    fun fromYearMonth(yearMonth: YearMonth?): String? =
        yearMonth?.toString()

    @TypeConverter
    fun toYearMonth(yearMonthString: String?): YearMonth? =
        yearMonthString?.let { YearMonth.parse(it) }
}
