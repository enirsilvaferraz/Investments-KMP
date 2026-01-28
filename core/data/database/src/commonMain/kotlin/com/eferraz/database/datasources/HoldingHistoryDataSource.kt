package com.eferraz.database.datasources

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public interface HoldingHistoryDataSource {
    public suspend fun upsert(entry: HoldingHistoryEntry): Long
    public suspend fun getAllHoldings(): List<AssetHolding>
    public suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry>
    public suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry?
    public suspend fun getByGoalAndReferenceDate(referenceDate: YearMonth, goalID: Long): List<HoldingHistoryEntry>
}

