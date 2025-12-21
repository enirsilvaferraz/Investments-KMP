package com.eferraz.presentation.features.history

import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

internal sealed class HistoryIntent {

    data class SelectPeriod(
        val period: YearMonth
    ) : HistoryIntent()
    
    data class UpdateEntryValue(
        val entry: HoldingHistoryEntry,
        val value: Double,
    ) : HistoryIntent()
    
    data object LoadInitialData : HistoryIntent()

    data object Sync : HistoryIntent()
}
