package com.eferraz.usecases.entities

import kotlinx.datetime.YearMonth

public enum class PeriodType {
    MENSAL,
    ANUAL,
}

/**
 * Representa uma linha da tabela de acompanhamento de metas.
 */
public data class GoalsMonitoringTableData(
    val monthYear: YearMonth,
    val goalValue: Double,
    val totalValue: Double,
    val contributions: Double,
    val withdrawals: Double,
    val growthValue: Double,
    val growthPercent: Double,
    val profitValue: Double,
    val profitPercent: Double,
    val balance: Double
)
