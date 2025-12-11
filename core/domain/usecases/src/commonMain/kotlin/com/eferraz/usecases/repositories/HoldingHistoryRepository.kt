package com.eferraz.usecases.repositories

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public interface HoldingHistoryRepository {
    public suspend fun getAllHoldings(): List<AssetHolding>
    public suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry>
    public suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry?
    public suspend fun update(entry: HoldingHistoryEntry)
    public suspend fun insert(entry: HoldingHistoryEntry): Long
}

