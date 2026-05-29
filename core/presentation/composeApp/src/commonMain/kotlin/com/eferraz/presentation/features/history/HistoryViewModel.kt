package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Growth
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.presentation.features.summary.SummaryProperties
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetCurrentYearMonthUseCase
import com.eferraz.usecases.cruds.GetFinancialGoalsUseCase
import com.eferraz.usecases.cruds.GetTransactionsUseCase
import com.eferraz.usecases.entities.HoldingHistoryView
import com.eferraz.usecases.screens.GetHistoryTableDataUseCase
import com.eferraz.usecases.services.ExportToCsvUseCase
import com.eferraz.usecases.services.ImportB3FileUseCase
import com.eferraz.usecases.services.SyncVariableIncomeValuesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    private val getCurrentYearMonthUseCase: GetCurrentYearMonthUseCase,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getGoalUseCase: GetFinancialGoalsUseCase,
    private val getHistoryTableDataUseCase: GetHistoryTableDataUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
    private val exportToCsvUseCase: ExportToCsvUseCase,
    private val importB3FileUseCase: ImportB3FileUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState> field = MutableStateFlow(HistoryState())

    init {

        viewModelScope.launch {

            val periods = async {
                getDataPeriodUseCase(Unit).getOrThrow()
            }

            val selected = async {
                getCurrentYearMonthUseCase(Unit).getOrThrow()
            }

            val brokerages = async {
                getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrThrow()
            }

            val goals = async {
                getGoalUseCase(GetFinancialGoalsUseCase.All).getOrThrow()
            }

            state.update {
                it.copy(
                    goal = it.goal.copy(options = goals.await()),
                    brokerage = it.brokerage.copy(options = brokerages.await()),
                    period = HistoryState.Choice(selected.await(), periods.await())
                )
            }

            processIntent(HistoryIntent.LoadInitialData)
        }
    }

    internal fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.LoadInitialData -> loadInitialData()
            is HistoryIntent.Sync -> sync()
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entry, intent.value)
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.SelectCategory -> selectCategory(intent.category)
            is HistoryIntent.SelectBrokerage -> selectBrokerage(intent.brokerage)
            is HistoryIntent.SelectLiquidity -> selectLiquidity(intent.liquidity)
            is HistoryIntent.SelectGoal -> selectGoal(intent.goal)
            is HistoryIntent.ExportFixedIncomeCsv -> exportFixedIncomeCsv()
            is HistoryIntent.ImportB3File -> importB3File()
        }
    }

    private fun selectPeriod(period: YearMonth) {
        state.update { it.copy(period = it.period.copy(selected = period)) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectCategory(category: InvestmentCategory) {
        val value = if (category == state.value.category.selected) null else category
        state.update { it.copy(category = it.category.copy(selected = value)) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectBrokerage(brokerage: Brokerage) {
        val value = if (brokerage == state.value.brokerage.selected) null else brokerage
        state.update { it.copy(brokerage = it.brokerage.copy(selected = value)) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectLiquidity(liquidity: Liquidity) {
        val value = if (liquidity == state.value.liquidity.selected) null else liquidity
        state.update { it.copy(liquidity = it.liquidity.copy(selected = value)) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectGoal(goal: FinancialGoal) {
        val value = if (goal == state.value.goal.selected) null else goal
        state.update { it.copy(goal = it.goal.copy(selected = value)) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun updateEntryValue(entry: HoldingHistoryEntry, value: Double) {
        viewModelScope.launch {
            updateFixedIncomeAndFundsHistoryValueUseCase(
                UpdateFixedIncomeAndFundsHistoryValueUseCase.Params(entry = entry, endOfMonthValue = value)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }.onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun sync() {
        viewModelScope.launch {
            // TODO mostrar progresso na tela
            updateVariableIncomeValues(
                SyncVariableIncomeValuesUseCase.Param(state.value.period.selected!!)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }.onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun exportFixedIncomeCsv() {
        viewModelScope.launch {
            exportToCsvUseCase(Unit).onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun importB3File() {
        state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            try {
                importB3FileUseCase(Unit)
            } finally {
                state.update { it.copy(isImporting = false) }
            }
        }
    }

    internal fun loadInitialData() {

        val period = state.value.period.selected!!
        val category = state.value.category.selected
        val brokerage = state.value.brokerage.selected
        val goal = state.value.goal.selected
        val liquidity = state.value.liquidity.selected

        viewModelScope.launch {

            val transactions = async {
                getTransactionsUseCase(
                    GetTransactionsUseCase.ReferenceDate(period)
                ).getOrNull() ?: emptyList()
            }

            val tableData = async {
                getHistoryTableDataUseCase(
                    GetHistoryTableDataUseCase.Param(period, category, brokerage, goal, liquidity)
                ).getOrNull() ?: emptyList()
            }

            state.update {

                val tableData1 = tableData.await().map { HoldingHistoryView(it) } // TODO Remover esse map
                val transactions1 = transactions.await()

                val previousValue = tableData1.sumOf { it.previousValue }
                val actualValue = tableData1.sumOf { it.currentValue }

                val (contributions, withdrawals, _) = TransactionBalance.calculate(transactions1)
                val (growth, growthPercent) = Growth.calculate(previousValue, actualValue, contributions, withdrawals)
                val (earnings, earningsPercent) = Appreciation.calculate(previousValue, actualValue, contributions, withdrawals)

                it.copy(
                    tableData = tableData1,
                    transactions = transactions1,
                    summaryProperties = SummaryProperties(
                        previousValue = previousValue,
                        actualValue = actualValue,
                        contributions = contributions,
                        withdrawals = withdrawals,
                        growth = growth,
                        growthPercent = growthPercent,
                        earnings = earnings,
                        earningsPercent = earningsPercent,
                    ),
                )
            }
        }
    }
}

internal sealed interface HistoryIntent {
    data object LoadInitialData : HistoryIntent
    data object Sync : HistoryIntent
    data object ExportFixedIncomeCsv : HistoryIntent
    data object ImportB3File : HistoryIntent
    data class UpdateEntryValue(val entry: HoldingHistoryEntry, val value: Double) : HistoryIntent
    data class SelectPeriod(val period: YearMonth) : HistoryIntent
    data class SelectBrokerage(val brokerage: Brokerage) : HistoryIntent
    data class SelectCategory(val category: InvestmentCategory) : HistoryIntent
    data class SelectLiquidity(val liquidity: Liquidity) : HistoryIntent
    data class SelectGoal(val goal: FinancialGoal) : HistoryIntent
}
