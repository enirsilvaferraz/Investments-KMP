package com.eferraz.design_system_v2.dateselector

import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

public object MonthYearLabelFormatter {

    private val monthNamesPtBr: Map<Month, String> =
        mapOf(
            Month.JANUARY to "Janeiro",
            Month.FEBRUARY to "Fevereiro",
            Month.MARCH to "Março",
            Month.APRIL to "Abril",
            Month.MAY to "Maio",
            Month.JUNE to "Junho",
            Month.JULY to "Julho",
            Month.AUGUST to "Agosto",
            Month.SEPTEMBER to "Setembro",
            Month.OCTOBER to "Outubro",
            Month.NOVEMBER to "Novembro",
            Month.DECEMBER to "Dezembro",
        )

    public fun format(yearMonth: YearMonth): String {
        val monthName = monthNamesPtBr.getValue(yearMonth.month)
        return "$monthName de ${yearMonth.year}"
    }
}
