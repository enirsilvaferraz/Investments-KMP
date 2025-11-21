package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
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
    private val repository: HoldingHistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(selectedPeriod = getCurrentYearMonth(), entries = emptyList()))
    val state = _state.asStateFlow()

    init {
        loadPeriodData(_state.value.selectedPeriod)
    }

    fun selectPeriod(period: YearMonth) {
        _state.update { it.copy(selectedPeriod = period) }
        loadPeriodData(period)
    }

//    fun updateEntryValue(entryId: Long?, holdingId: Long, value: Double, quantity: Double) {
//        viewModelScope.launch {
//            val currentPeriod = _state.value.selectedPeriod
//            val currentEntries = _state.value.currentPeriodEntries
//            val entry = if (entryId != null) {
//                currentEntries.firstOrNull { it.id == entryId }?.copy(
//                    endOfMonthValue = value,
//                    endOfMonthQuantity = quantity
//                )
//            } else {
//                // Criar novo entry
//                val state = _state.value
//                val holding = state.currentPeriodEntries.firstOrNull()?.holding
//                    ?: state.previousPeriodEntries.firstOrNull()?.holding
//                    ?: return@launch
//                HoldingHistoryEntry(
//                    id = 0,
//                    holding = holding,
//                    referenceDate = currentPeriod,
//                    endOfMonthValue = value,
//                    endOfMonthQuantity = quantity,
//                    endOfMonthAverageCost = 0.0,
//                    totalInvested = 0.0
//                )
//            } ?: return@launch
//
//            if (entryId != null) {
//                repository.update(entry)
//            } else {
//                repository.insert(entry)
//            }
//            // Recarregar dados
//            loadPeriodData(currentPeriod)
//        }
//    }

    private fun loadPeriodData(period: YearMonth) {
        viewModelScope.launch {
            repository.getByReferenceDateAndPrevious(period).collect { entries ->
                _state.update { it.copy(entries = entries) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentYearMonth(): YearMonth =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { now ->
            YearMonth(now.year, now.month)
        }

    data class HistoryState(
        val selectedPeriod: YearMonth,
        val entries: List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>,
    )
}

