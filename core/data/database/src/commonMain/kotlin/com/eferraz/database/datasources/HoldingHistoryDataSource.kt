package com.eferraz.database.datasources

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

public interface HoldingHistoryDataSource {
    public suspend fun insert(entry: HoldingHistoryEntry): Long
    public suspend fun update(entry: HoldingHistoryEntry)
    public fun getAllHoldings(): Flow<List<AssetHolding>>
    public fun getByReferenceDate(referenceDate: YearMonth): Flow<List<HoldingHistoryEntry>>
}

