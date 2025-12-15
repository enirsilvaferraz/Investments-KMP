package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
import com.eferraz.database.entities.assets.AssetWithDetails
import com.eferraz.database.mappers.toAssetWithDetails
import com.eferraz.database.mappers.toDomain
import com.eferraz.entities.Asset
import org.koin.core.annotation.Factory

@Factory(binds = [AssetDataSource::class])
internal class AssetDataSourceImpl(
    private val assetDao: AssetDao,
) : AssetDataSource {

    override suspend fun getAll(): List<Asset> =
        assetDao.getAll().map { it.toDomain() }

    override suspend fun getByID(id: Long): Asset? =
        assetDao.find(id)?.toDomain()

    override suspend fun save(asset: Asset): Long =
        assetDao.save(asset.toAssetWithDetails())
}