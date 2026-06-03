package com.eferraz.usecases.screens

import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Growth
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.entities.MonthSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Builds the consolidated month summary from filtered holding history entries.
 */
@Factory
public class GetMonthSummaryUseCase(
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetMonthSummaryUseCase.Param, MonthSummary>(context) {

    public data class Param(
        val referenceDate: YearMonth,
        val current: List<HoldingHistoryEntry>,
        val previous: List<HoldingHistoryEntry>,
    )

    override suspend fun execute(param: Param): MonthSummary {

        val previousByHoldingId = param.previous.associateBy { it.holding.id }

        var previousValue = 0.0
        var actualValue = 0.0
        var contributions = 0.0
        var withdrawals = 0.0

        for (entry in param.current) {

            actualValue += entry.marketValue()
            previousByHoldingId[entry.holding.id]?.let { previousValue += it.marketValue() }

            val monthTransactions = entry.holding.transactions.filter {
                it.date.year == param.referenceDate.year && it.date.month == param.referenceDate.month
            }

            val balance = TransactionBalance.calculate(monthTransactions)
            contributions += balance.contributions
            withdrawals += balance.withdrawals
        }

        val growth = Growth.calculate(
            previousValue = previousValue,
            currentValue = actualValue,
            contributions = contributions,
            withdrawals = withdrawals,
        )

        val earnings = Appreciation.calculate(
            previousValue = previousValue,
            currentValue = actualValue,
            contributions = contributions,
            withdrawals = withdrawals,
        )

        return MonthSummary(
            previousValue = previousValue,
            actualValue = actualValue,
            contributions = contributions,
            withdrawals = withdrawals,
            growth = growth.value,
            growthPercent = growth.percentage,
            earnings = earnings.value,
            earningsPercent = earnings.percentage,
        )
    }

    private fun HoldingHistoryEntry.marketValue(): Double =
        endOfMonthValue * endOfMonthQuantity
}
