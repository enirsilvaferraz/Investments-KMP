package com.eferraz.database.datasources

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

public interface HoldingHistoryDataSource {
    public suspend fun insert(entry: HoldingHistoryEntry): Long
    public suspend fun update(entry: HoldingHistoryEntry)
    public fun getByReferenceDateAndPrevious(referenceDate: YearMonth): Flow<List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>>
}

