package com.eferraz.usecases

import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.goals.GoalMonthlyData
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Growth
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.providers.DateProvider
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.yearMonth
import org.koin.core.annotation.Factory

/**
 * Use case responsável por calcular o histórico consolidado mensal de uma meta financeira.
 * Implements: [docs/RN - Calcular Histórico de Meta Financeira.md]
 *
 * Retorna um mapa de YearMonth para GoalMonthlyData, consolidando os dados de todas
 * as posições associadas à meta desde a startDate até o último mês completo.
 */
@Factory
public class GetGoalHistoryUseCase(
    private val historyRepository: HoldingHistoryRepository,
    private val transactionRepository: AssetTransactionRepository,
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetGoalHistoryUseCase.Param, Map<YearMonth, GoalMonthlyData>>(context) {

    public data class Param(
        val goal: FinancialGoal,
    ) {
        internal val startDate = goal.startDate.yearMonth
        internal val previousDate = startDate.minusMonth()
    }

    override suspend fun execute(param: Param): Map<YearMonth, GoalMonthlyData> {

        val months = SequenceMonths.build(param.previousDate, dateProvider.getCurrentYearMonth())
        var previous = emptyList<HoldingHistoryEntry>()

        return months.entries.associateWith { month ->

            val current = historyRepository.getByGoalAndReferenceDate(month, param.goal).groupBy { it.referenceDate }
            val currentValue = current[month]?.sumOf { it.endOfMonthValue } ?: 0.0

            val balance = TransactionBalance.calculate(
                transactions = transactionRepository.getByGoalAndReferenceDate(month, param.goal)
            )

            val data = if (month == param.previousDate) {
                GoalMonthlyData(
                    value = currentValue,
                    contributions = balance.contributions,
                    withdrawals = balance.withdrawals,
                    growth = 0.0,
                    growthRate = 0.0,
                    appreciation = 0.0,
                    appreciationRate = 0.0
                )
            } else {

                val previousValue = previous.sumOf { it.endOfMonthValue }

                val appreciation = Appreciation.calculate(
                    previousValue = previousValue,
                    currentValue = currentValue,
                    contributions = balance.contributions,
                    withdrawals = balance.withdrawals
                )

                val growth = Growth.calculate(
                    previousValue = previousValue,
                    currentValue = currentValue,
                    contributions = balance.contributions,
                    withdrawals = balance.withdrawals
                )

                GoalMonthlyData(
                    value = currentValue,
                    contributions = balance.contributions,
                    withdrawals = balance.withdrawals,
                    growth = growth.value,
                    growthRate = growth.percentage,
                    appreciation = appreciation.value,
                    appreciationRate = appreciation.percentage
                )
            }

            previous = current[month] ?: emptyList()

            data
        }
    }
}