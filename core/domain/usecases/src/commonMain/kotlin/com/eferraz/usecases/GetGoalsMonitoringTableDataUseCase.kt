package com.eferraz.usecases

import com.eferraz.entities.goals.GoalMonthlyData
import com.eferraz.entities.goals.GoalProjections
import com.eferraz.entities.goals.GoalProjectedValue
import com.eferraz.usecases.entities.GoalsMonitoringTableData
import com.eferraz.usecases.entities.PeriodType
import com.eferraz.usecases.repositories.GoalInvestmentPlanRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Use case responsável por obter os dados da tabela de acompanhamento de metas.
 * Combina dados históricos (passado) e projeções (futuro) da meta financeira.
 */
@Factory
public class GetGoalsMonitoringTableDataUseCase(
    private val goalInvestmentPlanRepository: GoalInvestmentPlanRepository,
    private val getGoalHistoryUseCase: GetGoalHistoryUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetGoalsMonitoringTableDataUseCase.Param, List<GoalsMonitoringTableData>>(context) {

    public data class Param(
        val goalId: Long,
        val periodType: PeriodType,
    )

    override suspend fun execute(param: Param): List<GoalsMonitoringTableData> {
        // 1. Obter histórico da meta
        val history = getGoalHistoryUseCase(GetGoalHistoryUseCase.Param(param.goalId))
            .getOrNull() ?: emptyMap()

        // 2. Obter projeções da meta
        val plan = goalInvestmentPlanRepository.getByGoalId(param.goalId) ?: return emptyList()
        val projections = GoalProjections.calculate(plan).projections

        // 3. Determinar último mês do histórico
        val lastHistoryMonth = history.keys.maxOrNull()

        // 4. Combinar mapas e gerar linhas da tabela
        val allMonths = (history.keys + projections.keys).sorted()

        return when (param.periodType) {
            PeriodType.MENSAL,
            PeriodType.ANUAL,
            -> allMonths.map { month ->
                val historicalData = history[month]
                val projectionData = projections[month]
                val isHistorical = lastHistoryMonth != null && month <= lastHistoryMonth

                if (isHistorical && historicalData != null) {
                    // Mês histórico: usar dados reais
                    historicalData.toTableData(
                        monthYear = month,
                        goalValue = projectionData?.projectedValue ?: 0.0
                    )
                } else if (projectionData != null) {
                    // Mês projetado: usar apenas goalValue
                    projectionData.toTableData(monthYear = month)
                } else {
                    // Fallback (não deveria acontecer)
                    GoalsMonitoringTableData(
                        monthYear = month,
                        goalValue = 0.0,
                        totalValue = 0.0,
                        balance = 0.0,
                        contributions = 0.0,
                        withdrawals = 0.0,
                        growthValue = 0.0,
                        growthPercent = 0.0,
                        profitValue = 0.0,
                        profitPercent = 0.0
                    )
                }
            }
        }
    }

    private fun GoalMonthlyData.toTableData(
        monthYear: YearMonth,
        goalValue: Double,
    ): GoalsMonitoringTableData =
        GoalsMonitoringTableData(
            monthYear = monthYear,
            goalValue = goalValue,
            totalValue = value,
            balance = contributions - withdrawals,
            contributions = contributions,
            withdrawals = withdrawals,
            growthValue = growth,
            growthPercent = growthRate,
            profitValue = appreciation,
            profitPercent = appreciationRate
        )

    private fun GoalProjectedValue.toTableData(monthYear: YearMonth): GoalsMonitoringTableData =
        GoalsMonitoringTableData(
            monthYear = monthYear,
            goalValue = projectedValue,
            totalValue = 0.0,
            balance = 0.0,
            contributions = 0.0,
            withdrawals = 0.0,
            growthValue = 0.0,
            growthPercent = 0.0,
            profitValue = 0.0,
            profitPercent = 0.0
        )
}
