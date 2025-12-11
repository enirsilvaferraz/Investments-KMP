package com.eferraz.repositories

import com.eferraz.database.datasources.HoldingHistoryDataSource
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryRepository::class])
internal class HoldingHistoryRepositoryImpl(
    private val dataSource: HoldingHistoryDataSource,
) : HoldingHistoryRepository {

    override suspend fun getAllHoldings(): List<AssetHolding> =
        dataSource.getAllHoldings()

    override suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry> =
        dataSource.getByReferenceDate(referenceDate)

    override suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? =
        dataSource.getByHoldingAndReferenceDate(referenceDate, holding)

    override suspend fun upsert(entry: HoldingHistoryEntry) =
        dataSource.upsert(entry)
}