package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.MergeHistoryUseCase
import com.eferraz.usecases.SyncVariableIncomeValuesUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.entities.HoldingHistoryResult
import com.eferraz.usecases.providers.DateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    dateProvider: DateProvider,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getHoldingHistoryUseCase: MergeHistoryUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(selectedPeriod = dateProvider.getCurrentYearMonth()))
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        processIntent(HistoryIntent.LoadInitialData)
    }

    fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entry, intent.value)
            is HistoryIntent.SelectHolding -> selectHolding(intent.holding)
            is HistoryIntent.LoadInitialData -> loadPeriodData()
            is HistoryIntent.Sync -> sync()
        }
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
                println("Success")
            }
        }
    }

    private fun loadPeriodData() {

        val period = _state.value.selectedPeriod

        viewModelScope.launch {

            getDataPeriodUseCase(Unit).onSuccess { entries ->
                _state.value = _state.value.copy(periods = entries)
            }

            getHoldingHistoryUseCase(MergeHistoryUseCase.Param(period)).onSuccess { entries ->
                _state.value = _state.value.copy(entries = entries)
            }.onFailure {
                println("Error: $it")
            }
        }
    }

    data class HistoryState(
        val selectedPeriod: YearMonth,
        val entries: List<HoldingHistoryResult> = emptyList(),
        val periods: List<YearMonth> = emptyList(),
        val selectedHolding: AssetHolding? = null,
    )
}

