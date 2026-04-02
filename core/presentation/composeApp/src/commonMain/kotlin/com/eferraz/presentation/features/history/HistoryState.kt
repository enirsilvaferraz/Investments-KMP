package com.eferraz.presentation.features.history

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.entities.HoldingHistoryView
import kotlinx.datetime.YearMonth

internal data class HistoryState(
    val tableData: List<HoldingHistoryView> = emptyList(),
    val selectedHolding: AssetHolding? = null,
    val period: Choice<YearMonth> = Choice(null, emptyList()),
    val brokerage: Choice<Brokerage> = Choice(null, emptyList()),
    val category: Choice<InvestmentCategory> = Choice(null, InvestmentCategory.entries),
    val liquidity: Choice<Liquidity> = Choice(null, Liquidity.entries),
    val goal: Choice<FinancialGoal> = Choice(null, emptyList()),
    val transactions: List<AssetTransaction> = emptyList(),
) {

    data class Choice<T>(
        val selected: T?,
        val options: List<T>,
    )
}
