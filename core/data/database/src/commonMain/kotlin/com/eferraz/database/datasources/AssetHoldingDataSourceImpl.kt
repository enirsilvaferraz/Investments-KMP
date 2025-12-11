package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.AssetHoldingEntity
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
        // Se tem ID, é atualização - preservar valores existentes
        val entity = if (assetHolding.id > 0) {
            val existingEntity = assetHoldingDao.getById(assetHolding.id)
            if (existingEntity != null) {
                // Preservar valores numéricos existentes, atualizar apenas relacionamentos
                AssetHoldingEntity(
                    id = assetHolding.id,
                    assetId = assetHolding.asset.id,
                    ownerId = assetHolding.owner.id,
                    brokerageId = assetHolding.brokerage.id,
                    quantity = existingEntity.quantity,
                    averageCost = existingEntity.averageCost,
                    investedValue = existingEntity.investedValue,
                    currentValue = existingEntity.currentValue
                )
            } else {
                // Se não encontrou, criar novo (não deveria acontecer, mas por segurança)
                assetHolding.toEntity()
            }
        } else {
            // Se não tem ID, é inserção
            assetHolding.toEntity()
        }

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
        brokerageId = brokerage.id,
        quantity = 0.0,
        averageCost = 0.0,
        investedValue = 0.0,
        currentValue = 0.0
    )
}

