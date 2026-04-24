package com.eferraz.asset_management.helpers

import kotlinx.datetime.LocalDate

private const val ISO_DATE_DIGIT_COUNT: Int = 8

@Deprecated("Pensar em uma forma padronizada... Usar o MaturityDate?")
internal fun localDateFromIsoDateDigits(digits: String?): LocalDate? =
    digits.takeIf { it.orEmpty().length == ISO_DATE_DIGIT_COUNT }?.let {
        LocalDate.Format {
            year()
            monthNumber()
            day()
        }.parse(it)
    }