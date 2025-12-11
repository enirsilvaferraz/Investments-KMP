package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.GetHoldingHistoryUseCase
import com.eferraz.usecases.HoldingHistoryResult
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import org.koin.android.annotation.KoinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@KoinViewModel
internal class HistoryViewModel(
    private val getHoldingHistoryUseCase: GetHoldingHistoryUseCase,
    private val repository: HoldingHistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HistoryState(
            selectedPeriod = getCurrentYearMonth(),
            periods = listOf(YearMonth(2025, 10), YearMonth(2025, 11), YearMonth(2025, 12), YearMonth(2026, 1), YearMonth(2026, 2))
        )
    )
    val state = _state.asStateFlow()

    init {
        loadPeriodData(_state.value.selectedPeriod)
    }

    fun selectPeriod(period: YearMonth) {
        _state.update { it.copy(selectedPeriod = period) }
        loadPeriodData(period)
    }

    fun updateEntryValue(entryId: Long?, holdingId: Long, value: Double) {
        viewModelScope.launch {
            val result = _state.value.entries.firstOrNull { it.holding.id == holdingId }
                ?: return@launch
            
            val entry = HoldingHistoryEntry(
                id = entryId,
                holding = result.holding,
                referenceDate = _state.value.selectedPeriod,
                endOfMonthValue = value,
                endOfMonthQuantity = 1.0,
                endOfMonthAverageCost = value
            )
            if (entryId == null) repository.insert(entry) else repository.update(entry)
            loadPeriodData(_state.value.selectedPeriod)
        }
    }

    private fun loadPeriodData(period: YearMonth) {
        viewModelScope.launch {
            val entries = getHoldingHistoryUseCase(period)
            _state.update { it.copy(entries = entries) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentYearMonth(): YearMonth =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { now ->
            YearMonth(now.year, now.month)
        }

    data class HistoryState(
        val selectedPeriod: YearMonth,
        val entries: List<HoldingHistoryResult> = emptyList(),
        val periods: List<YearMonth> = emptyList(),
    )
}

