package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.Brokerage
import com.eferraz.entities.Owner
import org.koin.core.annotation.Factory

@Factory(binds = [AssetHoldingDataSource::class])
internal class AssetHoldingDataSourceImpl(
    private val assetHoldingDao: AssetHoldingDao,
    private val assetDataSource: AssetDataSource,
    private val ownerDao: OwnerDao,
    private val brokerageDao: BrokerageDao,
) : AssetHoldingDataSource {

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

        return AssetHolding(
            id = entity.id,
            asset = asset,
            owner = Owner(id = ownerEntity.id, name = ownerEntity.name),
            brokerage = Brokerage(id = brokerageEntity.id, name = brokerageEntity.name)
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

    private suspend fun getHoldingsByAssets(assetsMap: Map<Long, Asset>): List<AssetHolding> {
        val holdingsWithDetails = assetHoldingDao.getAllWithAsset()

        return holdingsWithDetails.mapNotNull { holdingWithDetails ->
            val asset = assetsMap[holdingWithDetails.asset.id] ?: return@mapNotNull null
            AssetHolding(
                id = holdingWithDetails.holding.id,
                asset = asset,
                owner = Owner(id = holdingWithDetails.owner.id, name = holdingWithDetails.owner.name),
                brokerage = Brokerage(id = holdingWithDetails.brokerage.id, name = holdingWithDetails.brokerage.name)
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
        brokerageId = brokerage.id
    )
}

