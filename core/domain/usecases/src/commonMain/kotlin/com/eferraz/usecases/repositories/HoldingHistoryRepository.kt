package com.eferraz.usecases.repositories

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

public interface HoldingHistoryRepository {
    public fun getAllHoldings(): Flow<List<AssetHolding>>
    public fun getByReferenceDate(referenceDate: YearMonth): Flow<List<HoldingHistoryEntry>>
    public suspend fun update(entry: HoldingHistoryEntry)
    public suspend fun insert(entry: HoldingHistoryEntry): Long
}

