package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.screens.GetHistoryTableDataUseCase
import com.eferraz.usecases.services.SyncVariableIncomeValuesUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.providers.DateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    dateProvider: DateProvider,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getHistoryTableDataUseCase: GetHistoryTableDataUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(selectedPeriod = dateProvider.getCurrentYearMonth()))
    internal val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(periods = getDataPeriodUseCase(Unit).getOrNull() ?: emptyList())
        }

        processIntent(HistoryIntent.LoadInitialData)
    }

    internal fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entry, intent.value)
            is HistoryIntent.SelectHolding -> selectHolding(intent.holding)
            is HistoryIntent.LoadInitialData -> loadPeriodData()
            is HistoryIntent.Sync -> sync()
            is HistoryIntent.SelectCategory -> selectCategory(intent.category)
        }
    }

    private fun selectCategory(category: InvestmentCategory) {
        _state.update { it.copy(currentCategory = category) }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectPeriod(period: YearMonth) {
        _state.value = _state.value.copy(selectedPeriod = period)
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun selectHolding(holding: AssetHolding?) {
        _state.value = _state.value.copy(selectedHolding = holding)
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
                SyncVariableIncomeValuesUseCase.Param(_state.value.selectedPeriod)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }
        }
    }

    internal fun loadPeriodData() {

        val category = _state.value.currentCategory
        val period = _state.value.selectedPeriod

        viewModelScope.launch {
            getHistoryTableDataUseCase(GetHistoryTableDataUseCase.Param(period, category))
                .onSuccess { tableData -> _state.value = _state.value.copy(tableData = tableData) }
        }
    }

    internal sealed interface HistoryIntent {
        data class SelectPeriod(val period: YearMonth) : HistoryIntent
        data class UpdateEntryValue(val entry: HoldingHistoryEntry, val value: Double) : HistoryIntent
        data class SelectHolding(val holding: AssetHolding?) : HistoryIntent
        data object LoadInitialData : HistoryIntent
        data object Sync : HistoryIntent
        data class SelectCategory(val category: InvestmentCategory) : HistoryIntent
    }

    internal data class HistoryState(
        val selectedPeriod: YearMonth,
        val tableData: List<HistoryTableData> = emptyList(),
        val periods: List<YearMonth> = emptyList(),
        val selectedHolding: AssetHolding? = null,
        val currentCategory: InvestmentCategory = InvestmentCategory.FIXED_INCOME,
    )
}

