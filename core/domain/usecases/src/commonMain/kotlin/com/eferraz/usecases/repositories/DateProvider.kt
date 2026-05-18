package com.eferraz.usecases.repositories

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

public interface DateProvider {
    public fun getCurrentYearMonth(): YearMonth
    public fun getCurrentDate(): LocalDate
}
