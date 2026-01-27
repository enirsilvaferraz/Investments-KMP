package com.eferraz.usecases.repositories

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public interface HoldingHistoryRepository {
    public suspend fun getAllHoldings(): List<AssetHolding>
    public suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry>
    public suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry?
    public suspend fun upsert(entry: HoldingHistoryEntry): Long
}

