package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner
import org.koin.core.annotation.Factory

@Factory(binds = [AssetHoldingDataSource::class])
internal class AssetHoldingDataSourceImpl(
    private val assetHoldingDao: AssetHoldingDao,
    private val assetDataSource: AssetDataSource,
    private val ownerDao: OwnerDao,
    private val brokerageDao: BrokerageDao,
    private val financialGoalDataSource: FinancialGoalDataSource,
) : AssetHoldingDataSource {

    override suspend fun getById(holdingId: Long): AssetHolding = assetHoldingDao.getById(holdingId)?.let {

        val owner = ownerDao.getById(it.ownerId) ?: throw IllegalArgumentException("Owner not found")
        val brokerage = brokerageDao.getById(it.brokerageId) ?: throw IllegalArgumentException("Brokerage not found")

        AssetHolding(
            id = it.id,
            asset = assetDataSource.getByID(it.assetId) ?: throw IllegalArgumentException("Asset not found"),
            owner = Owner(id = owner.id, name = owner.name),
            brokerage = Brokerage(id = brokerage.id, name = brokerage.name),
            goal = it.goalId?.let { financialGoalDataSource.getById(it) }
        )

    } ?: throw IllegalArgumentException("Holding not found")

    override suspend fun save(assetHolding: AssetHolding): Long {
        val entity = assetHolding.toEntity()
        // @Upsert cuida de inserir ou atualizar automaticamente
        return assetHoldingDao.upsert(entity)
    }

    override suspend fun getByAssetId(assetId: Long): AssetHolding? {
        val entity = assetHoldingDao.getByAssetId(assetId) ?: return null

        // Buscar Asset completo
        val asset = assetDataSource.getByID(assetId) ?: return null

        // Buscar Owner e Brokerage
        val ownerEntity = ownerDao.getById(entity.ownerId) ?: return null
        val brokerageEntity = brokerageDao.getById(entity.brokerageId) ?: return null

        // Buscar Goal se existir
        val goal = entity.goalId?.let { financialGoalDataSource.getById(it) }

        return AssetHolding(
            id = entity.id,
            asset = asset,
            owner = Owner(id = ownerEntity.id, name = ownerEntity.name),
            brokerage = Brokerage(id = brokerageEntity.id, name = brokerageEntity.name),
            goal = goal
        )
    }

    override suspend fun getAll(): List<AssetHolding> {
        val assetsMap = assetDataSource.getAll().associateBy { asset -> asset.id }
        return getHoldingsByAssets(assetsMap)
    }

    override suspend fun getAllVariableIncomeAssets(): List<AssetHolding> {
        val assetsMap = assetDataSource.getAllVariableIncomeAssets().associateBy { asset -> asset.id }
        return getHoldingsByAssets(assetsMap)
    }

    override suspend fun getByCategory(category: InvestmentCategory): List<AssetHolding> {
        val assetsMap = assetDataSource.getByType(category).associateBy { asset -> asset.id }
        val holdingsWithDetails = assetHoldingDao.getAllWithAssetByCategory(category)

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            val goal = holdingWithDetails.holding.goalId?.let {
                financialGoalDataSource.getById(it)
            }
            AssetHolding(
                id = holdingWithDetails.holding.id,
                asset = assetsMap[holdingWithDetails.asset.id] ?: return@mapNotNull null,
                owner = Owner(id = holdingWithDetails.owner.id, name = holdingWithDetails.owner.name),
                brokerage = Brokerage(id = holdingWithDetails.brokerage.id, name = holdingWithDetails.brokerage.name),
                goal = goal
            )
        }
    }

    // TODO evitar chamadas ao banco de dados
    override suspend fun getByGoalId(goalId: Long): List<AssetHolding> {
        val assetsMap = assetDataSource.getAll().associateBy { asset -> asset.id }
        val holdingsWithDetails = assetHoldingDao.getAllWithAssetByGoalId(goalId)

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            val goal = holdingWithDetails.holding.goalId?.let {
                financialGoalDataSource.getById(it)
            }
            AssetHolding(
                id = holdingWithDetails.holding.id,
                asset = assetsMap[holdingWithDetails.asset.id] ?: return@mapNotNull null,
                owner = Owner(id = holdingWithDetails.owner.id, name = holdingWithDetails.owner.name),
                brokerage = Brokerage(id = holdingWithDetails.brokerage.id, name = holdingWithDetails.brokerage.name),
                goal = goal
            )
        }
    }

    private suspend fun getHoldingsByAssets(assetsMap: Map<Long, Asset>): List<AssetHolding> {
        val holdingsWithDetails = assetHoldingDao.getAllWithAsset()

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            val goal = holdingWithDetails.holding.goalId?.let {
                financialGoalDataSource.getById(it)
            }
            AssetHolding(
                id = holdingWithDetails.holding.id,
                asset = assetsMap[holdingWithDetails.asset.id] ?: return@mapNotNull null,
                owner = Owner(id = holdingWithDetails.owner.id, name = holdingWithDetails.owner.name),
                brokerage = Brokerage(id = holdingWithDetails.brokerage.id, name = holdingWithDetails.brokerage.name),
                goal = goal
            )
        }
    }

    override suspend fun delete(id: Long) {
        assetHoldingDao.deleteById(id)
    }

    private fun AssetHolding.toEntity() = AssetHoldingEntity(
        id = id,
        assetId = asset.id,
        ownerId = owner.id,
        brokerageId = brokerage.id,
        goalId = goal?.id
    )
}

