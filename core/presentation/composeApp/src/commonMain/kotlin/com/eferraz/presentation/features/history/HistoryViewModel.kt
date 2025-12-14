package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.HoldingHistoryResult
import com.eferraz.usecases.MergeHistoryUseCase
import com.eferraz.usecases.UpdateHistoryValueUseCase
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
    private val updateHistoryValueUseCase: UpdateHistoryValueUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState>
        field = MutableStateFlow(HistoryState(selectedPeriod = dateProvider.getCurrentYearMonth()))

    init {
        loadPeriodData(state.value.selectedPeriod)
    }

    fun selectPeriod(period: YearMonth) {
        state.update { it.copy(selectedPeriod = period) }
        loadPeriodData(period)
    }

    fun updateEntryValue(
        entry: HoldingHistoryEntry,
        value: Double,
    ) {

        viewModelScope.launch {

            updateHistoryValueUseCase(
                UpdateHistoryValueUseCase.Params(entry = entry, endOfMonthValue = value)
            ).onSuccess {
                loadPeriodData(state.value.selectedPeriod)
            }
        }
    }

    private fun loadPeriodData(period: YearMonth) {

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

