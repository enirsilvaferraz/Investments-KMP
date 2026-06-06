package com.eferraz.database.datasources.impl

import com.eferraz.database.daos.AssetHoldingDao
import androidx.room3.Transaction
import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.database.datasources.AssetHoldingDataSource
import com.eferraz.database.datasources.AssetTransactionDataSource
import com.eferraz.database.datasources.FinancialGoalDataSource
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.entities.holdings.AssetHoldingWithDetails
import com.eferraz.database.mappers.toDomain
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import org.koin.core.annotation.Factory

@Factory(binds = [AssetHoldingDataSource::class])
internal class AssetHoldingDataSourceImpl(
    private val assetHoldingDao: AssetHoldingDao,
    private val assetDataSource: AssetDataSource,
    private val financialGoalDataSource: FinancialGoalDataSource,
    private val assetTransactionDataSource: AssetTransactionDataSource,
) : AssetHoldingDataSource {

    override suspend fun getById(holdingId: Long): AssetHolding =
        assetHoldingDao.getByIdWithDetails(holdingId)?.let { mapHoldingWithDetails(it) }
            ?: throw IllegalArgumentException("Holding not found")

    override suspend fun save(assetHolding: AssetHolding): Long {
        val entity = assetHolding.toEntity()
        return assetHoldingDao.upsert(entity)
    }

    override suspend fun getByAssetId(assetId: Long): AssetHolding? =
        assetHoldingDao.getByAssetIdWithDetails(assetId)?.let { mapHoldingWithDetails(it) }

    override suspend fun getAll(): List<AssetHolding> {
        val assetsMap = assetDataSource.getAll().associateBy { asset -> asset.id }
        return getHoldingsByAssets(assetsMap)
    }

    override suspend fun getAllVariableIncomeAssets(): List<AssetHolding> {
        val assetsMap = assetDataSource.getAllVariableIncomeAssets().associateBy { asset -> asset.id }
        return getHoldingsByAssets(assetsMap)
    }

    override suspend fun getByAssetClass(assetClass: AssetClass): List<AssetHolding> {
        val assetsMap = assetDataSource.getByAssetClass(assetClass).associateBy { asset -> asset.id }
        val holdingsWithDetails = assetHoldingDao.getAllWithAssetByCategory(assetClass)

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            mapHoldingWithDetails(holdingWithDetails, assetsMap)
        }
    }

    override suspend fun getByGoalId(goalId: Long): List<AssetHolding> {
        val assetsMap = assetDataSource.getAll().associateBy { asset -> asset.id }
        val holdingsWithDetails = assetHoldingDao.getAllWithAssetByGoalId(goalId)

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            mapHoldingWithDetails(holdingWithDetails, assetsMap)
        }
    }

    private suspend fun getHoldingsByAssets(assetsMap: Map<Long, Asset>): List<AssetHolding> {
        val holdingsWithDetails = assetHoldingDao.getAllWithAsset()

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            mapHoldingWithDetails(holdingWithDetails, assetsMap)
        }
    }

    private suspend fun mapHoldingWithDetails(
        holdingWithDetails: AssetHoldingWithDetails,
        assetsMap: Map<Long, Asset>? = null,
    ): AssetHolding? {
        val asset = assetsMap?.get(holdingWithDetails.asset.id)
            ?: assetDataSource.getByID(holdingWithDetails.asset.id)
            ?: return null
        val goal = holdingWithDetails.holding.goalId?.let { financialGoalDataSource.getById(it) }
        return holdingWithDetails.toDomain(asset, goal)
    }

    override suspend fun delete(id: Long) {
        assetHoldingDao.deleteById(id)
    }

    @Transaction
    override suspend fun saveWithTransactions(assetHolding: AssetHolding) {
        val holdingId = save(assetHolding)
        val persisted = assetHolding.copy(id = holdingId)
        val existingIds = assetTransactionDataSource
            .getAllByHolding(persisted)
            .map { it.id }
            .toSet()
        val incomingIds = assetHolding.transactions.map { it.id }.toSet()
        val toDelete = existingIds - incomingIds
        toDelete.forEach { assetTransactionDataSource.delete(holdingId, it) }
        assetHolding.transactions.forEach { transaction ->
            assetTransactionDataSource.save(persisted, transaction)
        }
    }

    private fun AssetHolding.toEntity() =
        AssetHoldingEntity(
            id = id,
            assetId = asset.id,
            ownerId = owner.id,
            brokerageId = brokerage.id,
            goalId = goal?.id
        )
}
