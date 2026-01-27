package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.goals.GoalMonthlyData
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Growth
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.providers.DateProvider
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.FinancialGoalRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.plusMonth
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
    private val financialGoalRepository: FinancialGoalRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetGoalHistoryUseCase.Param, Map<YearMonth, GoalMonthlyData>>(context) {

    public data class Param(
        val goalId: Long,
    )

    override suspend fun execute(param: Param): Map<YearMonth, GoalMonthlyData> {

        // 1. Carregar meta pelo ID
        val goal = financialGoalRepository.getById(param.goalId) ?: return emptyMap()

        // 2. Buscar holdings da meta
        val holdings = assetHoldingRepository.getByGoalId(param.goalId)
        if (holdings.isEmpty()) return emptyMap()

        // 3. Determinar período: desde startDate até último mês completo
        val startYearMonth = goal.startDate.yearMonth
        val currentYearMonth = dateProvider.getCurrentYearMonth()
        val lastCompleteMonth = currentYearMonth.minusMonth() // Último mês completo

        // 4. Gerar lista de meses a processar
        val months = generateMonths(startYearMonth, lastCompleteMonth)

        // 5. Para cada mês, consolidar dados de todos os holdings
        val historyMap = mutableMapOf<YearMonth, GoalMonthlyData>()

        for (month in months) {
            historyMap[month] = calculateMonthlyData(month, holdings, startYearMonth)
        }

        return historyMap
    }

    /**
     * Calcula os dados consolidados de um mês específico para todos os holdings da meta.
     */
    private suspend fun calculateMonthlyData(
        month: YearMonth,
        holdings: List<AssetHolding>,
        startYearMonth: YearMonth,
    ): GoalMonthlyData {

        var totalValue = 0.0
        var totalContributions = 0.0
        var totalWithdrawals = 0.0
        var totalAppreciation = 0.0
        var totalGrowth = 0.0

        // Calcular período do mês para buscar transações
        val startDate = LocalDate(month.year, month.month, 1)
        val endDate = startDate.plus(DatePeriod(months = 1)).plus(DatePeriod(days = -1))

        // Para cada holding, obter dados do mês
        for (holding in holdings) {

            val currentHistory = holdingHistoryRepository.getByHoldingAndReferenceDate(month, holding)
            val previousMonth = month.minusMonth()
            val previousHistory = holdingHistoryRepository.getByHoldingAndReferenceDate(previousMonth, holding)

            // Se não houver histórico atual, o holding não contribui para este mês
            if (currentHistory == null) continue

            // Obter transações do mês
            val transactions = assetTransactionRepository.getAllByHoldingAndDateRange(holding, startDate, endDate)

            // Calcular balanço de transações
            val transactionBalance = TransactionBalance.calculate(transactions)

            // Calcular apreciação
            val appreciation = Appreciation.calculate(
                previousValue = previousHistory?.endOfMonthValue ?: 0.0,
                currentValue = currentHistory.endOfMonthValue,
                contributions = transactionBalance.contributions,
                withdrawals = transactionBalance.withdrawals
            )

            // Calcular crescimento
            val growth = Growth.calculate(
                previousValue = previousHistory?.endOfMonthValue ?: 0.0,
                currentValue = currentHistory.endOfMonthValue,
                contributions = transactionBalance.contributions,
                withdrawals = transactionBalance.withdrawals
            )

            // Consolidar valores
            totalValue += currentHistory.endOfMonthValue
            totalContributions += transactionBalance.contributions
            totalWithdrawals += transactionBalance.withdrawals
            totalAppreciation += appreciation.value
            totalGrowth += growth.value
        }

        // Calcular valor do mês anterior (consolidado)
        val previousMonth = month.minusMonth()

        val previousValue =
            if (previousMonth < startYearMonth) 0.0
            else calculatePreviousMonthValue(previousMonth, holdings)

        // Calcular percentuais consolidados
        val appreciationRate = if (previousValue + totalContributions > 0) (totalAppreciation / (previousValue + totalContributions)) * 100 else 0.0
        val growthRate = if (previousValue > 0) (totalGrowth / previousValue) * 100 else 0.0

        return GoalMonthlyData(
            value = totalValue,
            contributions = totalContributions,
            withdrawals = totalWithdrawals,
            growth = totalGrowth,
            growthRate = growthRate,
            appreciation = totalAppreciation,
            appreciationRate = appreciationRate
        )
    }

    /**
     * Calcula o valor consolidado do mês anterior.
     */
    private suspend fun calculatePreviousMonthValue(
        previousMonth: YearMonth,
        holdings: List<AssetHolding>,
    ): Double {
        var totalValue = 0.0
        for (holding in holdings) {
            val previousHistory = holdingHistoryRepository.getByHoldingAndReferenceDate(previousMonth, holding)
            if (previousHistory != null) {
                totalValue += previousHistory.endOfMonthValue
            }
        }
        return totalValue
    }

    /**
     * Gera lista de meses entre start e end (inclusive).
     */
    private fun generateMonths(start: YearMonth, end: YearMonth): List<YearMonth> {
        val months = mutableListOf<YearMonth>()
        var current = start
        while (current <= end) {
            months.add(current)
            current = current.plusMonth()
        }
        return months
    }
}
