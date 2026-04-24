package com.eferraz.asset_management

import kotlinx.datetime.LocalDate

private const val ISO_DATE_DIGIT_COUNT: Int = 8
private const val YEAR_END_EXCLUSIVE: Int = 4
private const val MONTH_START: Int = 4
private const val MONTH_END_EXCLUSIVE: Int = 6
private const val DAY_START: Int = 6

/**
 * Parses [digits] as YYYYMMDD when it has exactly eight digits and represents a valid calendar date.
 */
internal fun localDateFromIsoDateDigits(digits: String?): LocalDate? =
    if (digits.orEmpty().length != ISO_DATE_DIGIT_COUNT) {
        null
    } else {
        val y = digits.orEmpty().substring(0, YEAR_END_EXCLUSIVE).toIntOrNull()
        val month = digits.orEmpty().substring(MONTH_START, MONTH_END_EXCLUSIVE).toIntOrNull()
        val day = digits.orEmpty().substring(DAY_START, ISO_DATE_DIGIT_COUNT).toIntOrNull()
        if (y == null || month == null || day == null) {
            null
        } else {
            runCatching { LocalDate(y, month, day) }.getOrNull()
        }
    }
