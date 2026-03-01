package com.eferraz.usecases.entities

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.holdings.HoldingHistoryEntry

public data class HoldingHistoryView(
    val entry: HoldingHistoryEntry,
    val category: InvestmentCategory,
    val brokerageName: String,
    val issuerName: String,
    val displayName: String,
    val liquidity: Liquidity?,
    val observations: String,
    val previousValue: Double,
    val currentValue: Double,
    val appreciation: Double,
    val totalBalance: Double,
) {

    public constructor(it: HistoryTableData) : this(
        entry = it.currentEntry,
        category = it.category,
        brokerageName = it.brokerageName,
        issuerName = it.issuerName,
        displayName = it.displayName,
        liquidity = if (it is FixedIncomeHistoryTableData) it.liquidity else null,
        observations = it.observations,
        previousValue = it.previousValue,
        currentValue = it.currentValue,
        appreciation = it.appreciation,
        totalBalance = it.totalBalance
    )

    public fun isCurrentValueEnabled(): Boolean = category != InvestmentCategory.VARIABLE_INCOME
}