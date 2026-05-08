package com.eferraz.filestore

import com.eferraz.entities.holdings.HoldingHistoryEntry

public interface AssetFileStore {
    public fun exportToCSV(data: List<HoldingHistoryEntry>)
}