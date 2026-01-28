package com.eferraz.database.datasources

import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.entities.histories.HoldingHistoryEntryEntity
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryDataSource::class])
internal class HoldingHistoryDataSourceImpl(
    private val holdingHistoryDao: HoldingHistoryDao,
    private val assetHoldingDataSource: AssetHoldingDataSource,
) : HoldingHistoryDataSource {

    override suspend fun getAllHoldings(): List<AssetHolding> {
        return assetHoldingDataSource.getAll()
    }

    override suspend fun getByReferenceDate(referenceDate: YearMonth) =
        holdingHistoryDao.getByReferenceDate(referenceDate).map { it.history.toModel() }

    override suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {
        val historyWithDetails = holdingHistoryDao.getByHoldingAndReferenceDate(referenceDate, holding.id) ?: return null
        return historyWithDetails.history.toModel()
    }

    override suspend fun getByGoalAndReferenceDate(referenceDate: YearMonth, goalID: Long) =
        holdingHistoryDao.getByGoalAndReferenceDate(referenceDate, goalID).map { it.history.toModel() }

    override suspend fun upsert(entry: HoldingHistoryEntry): Long =
        holdingHistoryDao.upsert(entry.toEntity())

    private fun HoldingHistoryEntry.toEntity() =
        HoldingHistoryEntryEntity(
            id = id,
            holdingId = holding.id,
            referenceDate = referenceDate,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity,
            endOfMonthAverageCost = endOfMonthAverageCost,
            totalInvested = totalInvested
        )

    private suspend fun HoldingHistoryEntryEntity.toModel() =
        HoldingHistoryEntry(
            id = id ?: 0,
            holding = assetHoldingDataSource.getById(holdingId),
            referenceDate = referenceDate,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity,
            endOfMonthAverageCost = endOfMonthAverageCost,
            totalInvested = totalInvested
        )
}