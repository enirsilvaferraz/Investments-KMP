package com.eferraz.repositories

import com.eferraz.database.datasources.AssetHoldingDataSource
import com.eferraz.entities.AssetHolding
import com.eferraz.usecases.repositories.AssetHoldingRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetHoldingRepository::class])
internal class AssetHoldingRepositoryImpl(
    private val dataSource: AssetHoldingDataSource,
) : AssetHoldingRepository {

    override suspend fun save(assetHolding: AssetHolding) = dataSource.save(assetHolding)

    override suspend fun getByAssetId(assetId: Long) = dataSource.getByAssetId(assetId)

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun delete(id: Long) = dataSource.delete(id)
}

