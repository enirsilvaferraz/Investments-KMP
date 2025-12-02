package com.eferraz.database.datasources

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public interface HoldingHistoryDataSource {
    public suspend fun insert(entry: HoldingHistoryEntry): Long
    public suspend fun update(entry: HoldingHistoryEntry)
    public suspend fun getAllHoldings(): List<AssetHolding>
    public suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry>
}

