package com.eferraz.usecases.screens

import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.goals.GoalMonthlyData
import com.eferraz.entities.goals.GoalProjections
import com.eferraz.entities.goals.GrowthRate
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.GetGoalHistoryUseCase
import com.eferraz.usecases.entities.GoalsMonitoringTableData
import com.eferraz.usecases.entities.PeriodType
import com.eferraz.usecases.repositories.GoalInvestmentPlanRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.until
import kotlinx.datetime.yearMonth
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
        val goal: FinancialGoal,
        val periodType: PeriodType,
    )

    override suspend fun execute(param: Param): List<GoalsMonitoringTableData> {

        val plan = goalInvestmentPlanRepository.getByGoal(param.goal.id) ?: return emptyList()

        val history = getGoalHistoryUseCase(GetGoalHistoryUseCase.Param(param.goal)).getOrNull() ?: emptyMap()

        val planedProjections = GoalProjections.Companion.calculate(
            startMonth = plan.goal.startDate.yearMonth,
            initialValue = history[plan.goal.startDate.yearMonth.minusMonth()]?.value ?: 0.0,
            appreciationRate = plan.appreciationRate,
            contribution = plan.contribution,
            targetValue = plan.goal.targetValue
        ).map

        val growthRate = GrowthRate.Companion.calculate(
            initialValue = history.values.first().value,
            finalValue = history.values.last().value,
            periods = history.keys.first().until(history.keys.last(), DateTimeUnit.Companion.MONTH).toInt()
        )

        val calculatedProjections = GoalProjections.Companion.calculate(
            initialValue = history.values.first().value,
            startMonth = history.keys.last(),
            appreciationRate = growthRate.percentValue,
            contribution = 0.0,
            targetValue = plan.goal.targetValue
        ).map

        return planedProjections.keys.map { month ->

            val historicalData = history[month] ?: GoalMonthlyData(
                value = calculatedProjections[month]?.value ?: 0.0,
                contributions = 0.0,
                withdrawals = 0.0,
                growth = 0.0,
                growthRate = 0.0,
                appreciation = 0.0,
                appreciationRate = 0.0
            )

            historicalData.toTableData(monthYear = month, goalValue = planedProjections[month]?.value ?: 0.0)
        }
    }

    private fun GoalMonthlyData.toTableData(monthYear: YearMonth, goalValue: Double): GoalsMonitoringTableData =
        GoalsMonitoringTableData(
            monthYear = monthYear,
            goalValue = goalValue,
            totalValue = value,
            contributions = contributions,
            withdrawals = withdrawals,
            growthValue = growth,
            growthPercent = growthRate,
            profitValue = appreciation,
            profitPercent = appreciationRate,
            balance = if (value != 0.0) value - goalValue else 0.0
        )
}