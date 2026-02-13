package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetFinancialGoalsUseCase
import com.eferraz.usecases.cruds.GetTransactionsUseCase
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.providers.DateProvider
import com.eferraz.usecases.screens.GetHistoryTableDataUseCase
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
    dateProvider: DateProvider,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getGoalUseCase: GetFinancialGoalsUseCase,
    private val getHistoryTableDataUseCase: GetHistoryTableDataUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState> field = MutableStateFlow(
        HistoryState(
            period = HistoryState.Choice(
                dateProvider.getCurrentYearMonth(),
                emptyList()
            ),
        )
    )

    init {

        viewModelScope.launch {

            val periods = async {
                getDataPeriodUseCase(Unit).getOrThrow()
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
                    period = it.period.copy(options = periods.await())
                )
            }
        }

        processIntent(HistoryIntent.LoadInitialData)
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

    private fun updateEntryValue(
        entry: HoldingHistoryEntry,
        value: Double,
    ) {
        viewModelScope.launch {
            updateFixedIncomeAndFundsHistoryValueUseCase(
                UpdateFixedIncomeAndFundsHistoryValueUseCase.Params(entry = entry, endOfMonthValue = value)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }
        }
    }

    private fun sync() {
        viewModelScope.launch {
            updateVariableIncomeValues(
                SyncVariableIncomeValuesUseCase.Param(state.value.period.selected!!)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
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
                    GetHistoryTableDataUseCase.Param(
                        referenceDate = period,
                        category = category,
                        brokerage = brokerage,
                        goal = goal,
                        liquidity = liquidity
                    )
                ).getOrNull() ?: emptyList()
            }

            state.update { it.copy(tableData = tableData.await(), transactions = transactions.await()) }
        }
    }

    internal sealed interface HistoryIntent {
        data object LoadInitialData : HistoryIntent
        data object Sync : HistoryIntent
        data class UpdateEntryValue(val entry: HoldingHistoryEntry, val value: Double) : HistoryIntent
        data class SelectPeriod(val period: YearMonth) : HistoryIntent
        data class SelectBrokerage(val brokerage: Brokerage) : HistoryIntent
        data class SelectCategory(val category: InvestmentCategory) : HistoryIntent
        data class SelectLiquidity(val liquidity: Liquidity) : HistoryIntent
        data class SelectGoal(val goal: FinancialGoal) : HistoryIntent
    }

    internal data class HistoryState(
        val tableData: List<HistoryTableData> = emptyList(),
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
}

