package com.eferraz.database.datasources

import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
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

    override suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry> {
        val holdingsMap = assetHoldingDataSource.getAll().associateBy { holding -> holding.id }
        val historyWithDetails = holdingHistoryDao.getByReferenceDate(referenceDate)

        return historyWithDetails.mapNotNull {
            val holding = holdingsMap[it.holding.id] ?: return@mapNotNull null
            it.history.toModel(holding)
        }
    }

    override suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {
        val historyWithDetails = holdingHistoryDao.getByHoldingAndReferenceDate(referenceDate, holding.id) ?: return null
        return historyWithDetails.history.toModel(holding)
    }

    override suspend fun update(entry: HoldingHistoryEntry) {
        holdingHistoryDao.update(entry.toEntity())
    }

    override suspend fun insert(entry: HoldingHistoryEntry): Long =
        holdingHistoryDao.insert(entry.toEntity())

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

    private fun HoldingHistoryEntryEntity.toModel(holding: AssetHolding) =
        HoldingHistoryEntry(
            id = id ?: 0,
            holding = holding,
            referenceDate = referenceDate,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity,
            endOfMonthAverageCost = endOfMonthAverageCost,
            totalInvested = totalInvested
        )
}