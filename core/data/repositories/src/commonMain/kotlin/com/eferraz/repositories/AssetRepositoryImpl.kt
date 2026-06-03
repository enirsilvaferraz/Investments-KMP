package com.eferraz.repositories

import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetDataSource,
) : AssetRepository {

    override suspend fun getAll() =
        dataSource.getAll()

    override suspend fun delete(id: Long) =
        dataSource.delete(id)

    override suspend fun getByAssetClass(assetClass: AssetClass) =
        dataSource.getByAssetClass(assetClass)

    override suspend fun getById(id: Long) =
        dataSource.getByID(id)

    override suspend fun getByTicker(ticker: String) =
        dataSource.getByTicker(ticker)

    override suspend fun upsert(model: Asset) =
        dataSource.save(model)
}
