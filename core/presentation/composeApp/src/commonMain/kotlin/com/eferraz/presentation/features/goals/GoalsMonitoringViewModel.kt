package com.eferraz.presentation.features.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.cruds.GetFinancialGoalsUseCase
import com.eferraz.usecases.screens.GetGoalsMonitoringTableDataUseCase
import com.eferraz.usecases.entities.GoalsMonitoringTableData
import com.eferraz.usecases.entities.PeriodType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class GoalsMonitoringViewModel(
    private val getFinancialGoalsUseCase: GetFinancialGoalsUseCase,
    private val getGoalsMonitoringTableDataUseCase: GetGoalsMonitoringTableDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsMonitoringState())
    internal val state: StateFlow<GoalsMonitoringState> = _state.asStateFlow()

    init {
        loadGoals()
    }

    internal fun processIntent(intent: GoalsMonitoringIntent) {
        when (intent) {
            is GoalsMonitoringIntent.SelectGoal -> selectGoal(intent.goal)
            is GoalsMonitoringIntent.SelectPeriodType -> selectPeriodType(intent.periodType)
        }
    }

    private fun selectGoal(goal: FinancialGoal) {
        _state.update {
            it.copy(
                selectedGoal = goal,
            )
        }
        loadTableData(goal, _state.value.periodType)
    }

    private fun selectPeriodType(periodType: PeriodType) {
        _state.update { it.copy(periodType = periodType) }
        _state.value.selectedGoal?.let { loadTableData(it, periodType) }
    }

    private fun loadGoals() {
        viewModelScope.launch {
            val goals = getFinancialGoalsUseCase(GetFinancialGoalsUseCase.All).getOrNull().orEmpty()
            val selectedGoal = goals.firstOrNull()
            _state.update { it.copy(goals = goals, selectedGoal = selectedGoal) }
            selectedGoal?.let { loadTableData(it, _state.value.periodType) }
        }
    }

    private fun loadTableData(goal: FinancialGoal, periodType: PeriodType) {
        viewModelScope.launch {
            getGoalsMonitoringTableDataUseCase(
                GetGoalsMonitoringTableDataUseCase.Param(goal = goal, periodType = periodType)
            )
                .onFailure {
                    println(it)
                }
                .onSuccess { tableData ->
                    _state.update {
                        it.copy(
                            historyData = tableData,
                            goalDetails = buildGoalDetails(goal, tableData)
                        )
                    }
                }
        }
    }

    private fun buildGoalDetails(
        goal: FinancialGoal,
        tableData: List<GoalsMonitoringTableData>,
    ): GoalDetails {
        val lastProjection = tableData.maxByOrNull { it.monthYear }?.monthYear?.formated() ?: "-"
        return GoalDetails(
            name = goal.name,
            targetValue = goal.targetValue,
            targetDate = lastProjection,
            startDate = goal.startDate.formated(),
            expectedMonthlyContribution = 0.0,
            expectedAnnualReturn = 0.0,
            linkedAssets = emptyList()
        )
    }

    internal sealed interface GoalsMonitoringIntent {
        data class SelectGoal(val goal: FinancialGoal) : GoalsMonitoringIntent
        data class SelectPeriodType(val periodType: PeriodType) : GoalsMonitoringIntent
    }

    internal data class GoalsMonitoringState(
        val selectedGoal: FinancialGoal? = null,
        val goals: List<FinancialGoal> = emptyList(),
        val periodType: PeriodType = PeriodType.MENSAL,
        val historyData: List<GoalsMonitoringTableData> = emptyList(),
        val goalDetails: GoalDetails? = null,
    )
}

internal data class GoalDetails(
    val name: String,
    val targetValue: Double,
    val targetDate: String,
    val startDate: String,
    val expectedMonthlyContribution: Double,
    val expectedAnnualReturn: Double,
    val linkedAssets: List<String>,
)
