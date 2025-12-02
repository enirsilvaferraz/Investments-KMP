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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryDataSource::class])
internal class HoldingHistoryDataSourceImpl(
    private val assetHoldingDao: AssetHoldingDao,
    private val holdingHistoryDao: HoldingHistoryDao,
    private val assetDataSource: AssetDataSource,
) : HoldingHistoryDataSource {

    override fun getAllHoldings(): Flow<List<AssetHolding>> {
        return combine(
            assetDataSource.getAll().map { it.associateBy { asset -> asset.id } },
            assetHoldingDao.getAllWithAsset()
        ) { assetsMap, holdingsWithDetails ->
            holdingsWithDetails.map { it.toHoldingModel(assetsMap[it.asset.id]!!) }
        }
    }

    override fun getByReferenceDate(referenceDate: YearMonth): Flow<List<HoldingHistoryEntry>> {
        return combine(
            getAllHoldings().map { it.associateBy { holding -> holding.id } },
            holdingHistoryDao.getByReferenceDate(referenceDate)
        ) { holdingsMap, historyWithDetails ->
            historyWithDetails.map {
                val holding = holdingsMap[it.holding.id]!!
                it.history.toModel(holding)
            }
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