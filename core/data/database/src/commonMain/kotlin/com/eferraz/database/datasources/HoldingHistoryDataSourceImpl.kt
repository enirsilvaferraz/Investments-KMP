package com.eferraz.database.datasources

import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.entities.HoldingHistoryEntryEntity
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
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryDataSource::class])
internal class HoldingHistoryDataSourceImpl(
    private val holdingHistoryDao: HoldingHistoryDao,
    private val assetDataSource: AssetDataSource,
) : HoldingHistoryDataSource {

    override fun getByReferenceDateAndPrevious(referenceDate: YearMonth): Flow<List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>> {

        return combine(
            assetDataSource.getAll().map { it.associateBy { asset -> asset.id } },
            holdingHistoryDao.getByReferenceDate(referenceDate),
            holdingHistoryDao.getByReferenceDate(referenceDate.minusMonth())
        ) { assets: Map<Long, Asset>, current: List<HoldingHistoryWithDetails>, previous: List<HoldingHistoryWithDetails> ->

            val triples = hashMapOf<AssetHolding, Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>()

            current.forEach {
                val asset = assets[it.asset.id]!!
                val holding = it.toHoldingModel(asset)
                val entry = it.history.toModel(holding)

                if (triples.containsKey(holding)) {
                    triples[holding] = triples[holding]!!.copy(second = entry)
                } else {
                    triples[holding] = Triple(holding, entry, null)
                }
            }

            previous.forEach {
                val asset = assets[it.asset.id]!!
                val holding = it.toHoldingModel(asset)
                val entry = it.history.toModel(holding)

                if (triples.containsKey(holding)) {
                    triples[holding] = triples[holding]!!.copy(third = entry)
                } else {
                    triples[holding] = Triple(holding, null, entry)
                }
            }

            triples.map { it.value }
        }
    }

    override suspend fun update(entry: HoldingHistoryEntry) {
        val entity = HoldingHistoryEntryEntity(
            id = entry.id,
            holdingId = entry.holding.id,
            referenceDate = entry.referenceDate,
            endOfMonthValue = entry.endOfMonthValue,
            endOfMonthQuantity = entry.endOfMonthQuantity,
            endOfMonthAverageCost = entry.endOfMonthAverageCost,
            totalInvested = entry.totalInvested
        )
        holdingHistoryDao.update(entity)
    }

    override suspend fun insert(entry: HoldingHistoryEntry): Long {
        val entity = HoldingHistoryEntryEntity(
            id = 0,
            holdingId = entry.holding.id,
            referenceDate = entry.referenceDate,
            endOfMonthValue = entry.endOfMonthValue,
            endOfMonthQuantity = entry.endOfMonthQuantity,
            endOfMonthAverageCost = entry.endOfMonthAverageCost,
            totalInvested = entry.totalInvested
        )
        return holdingHistoryDao.insert(entity)
    }

    private fun HoldingHistoryWithDetails.toHoldingModel(asset: Asset) = AssetHolding(
        id = holding.id,
        asset = asset,
        owner = Owner(id = owner.id, name = owner.name),
        brokerage = Brokerage(id = brokerage.id, name = brokerage.name)
    )

    private fun HoldingHistoryEntryEntity.toModel(holding: AssetHolding) = HoldingHistoryEntry(
        id = id,
        holding = holding,
        referenceDate = referenceDate,
        endOfMonthValue = endOfMonthValue,
        endOfMonthQuantity = endOfMonthQuantity,
        endOfMonthAverageCost = endOfMonthAverageCost,
        totalInvested = totalInvested
    )
}