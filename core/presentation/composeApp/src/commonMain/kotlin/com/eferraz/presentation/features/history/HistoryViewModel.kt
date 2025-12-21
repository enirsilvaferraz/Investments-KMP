package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.HoldingHistoryResult
import com.eferraz.usecases.MergeHistoryUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.UpdateVariableIncomeValuesUseCase
import com.eferraz.usecases.providers.DateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    dateProvider: DateProvider,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getHoldingHistoryUseCase: MergeHistoryUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: UpdateVariableIncomeValuesUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState>
        field = MutableStateFlow(HistoryState(selectedPeriod = dateProvider.getCurrentYearMonth()))

    init {
        processIntent(HistoryIntent.LoadInitialData)
    }

    fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entry, intent.value)
            is HistoryIntent.LoadInitialData -> loadPeriodData()
            is HistoryIntent.Sync -> sync()
        }
    }

    private fun selectPeriod(period: YearMonth) {
        state.value = state.value.copy(selectedPeriod = period)
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
                UpdateVariableIncomeValuesUseCase.Param(state.value.selectedPeriod)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
                println("Success")
            }
        }
    }

    private fun loadPeriodData() {

        val period = state.value.selectedPeriod

        viewModelScope.launch {

            getDataPeriodUseCase(Unit).onSuccess { entries ->
                state.update { it.copy(periods = entries) }
            }

            getHoldingHistoryUseCase(MergeHistoryUseCase.Param(period)).onSuccess { entries ->
                state.update { it.copy(entries = entries) }
            }
        }
    }

    data class HistoryState(
        val selectedPeriod: YearMonth,
        val entries: List<HoldingHistoryResult> = emptyList(),
        val periods: List<YearMonth> = emptyList(),
    )
}

