package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.database.entities.relationship.AssetHoldingWithDetails
import com.eferraz.database.entities.relationship.HoldingHistoryWithDetails
import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.Brokerage
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.Owner
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryDataSource::class])
internal class HoldingHistoryDataSourceImpl(
    private val assetHoldingDao: AssetHoldingDao,
    private val holdingHistoryDao: HoldingHistoryDao,
    private val assetDataSource: AssetDataSource,
) : HoldingHistoryDataSource {

    override suspend fun getAllHoldings(): List<AssetHolding> {
        val assetsMap = assetDataSource.getAll().associateBy { asset -> asset.id }
        val holdingsWithDetails = assetHoldingDao.getAllWithAsset()
        
        return holdingsWithDetails.map { it.toHoldingModel(assetsMap[it.asset.id]!!) }
    }

    override suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryEntry> {
        val holdingsMap = getAllHoldings().associateBy { holding -> holding.id }
        val historyWithDetails = holdingHistoryDao.getByReferenceDate(referenceDate)
        
        return historyWithDetails.map {
            val holding = holdingsMap[it.holding.id]!!
            it.history.toModel(holding)
        }
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

    private fun AssetHoldingWithDetails.toHoldingModel(asset: Asset) =
        AssetHolding(
            id = holding.id,
            asset = asset,
            owner = Owner(id = owner.id, name = owner.name),
            brokerage = Brokerage(id = brokerage.id, name = brokerage.name)
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