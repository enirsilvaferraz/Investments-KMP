package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
import com.eferraz.database.mappers.toDomain
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.Asset
import com.eferraz.entities.InvestmentCategory
import org.koin.core.annotation.Factory

@Factory(binds = [AssetDataSource::class])
internal class AssetDataSourceImpl(
    private val assetDao: AssetDao,
) : AssetDataSource {

    override suspend fun getAll(): List<Asset> =
        assetDao.getAll().map { it.toDomain() }

    override suspend fun getByType(category: InvestmentCategory) =
        assetDao.getByType(category).map { it.toDomain() }

    override suspend fun getAllVariableIncomeAssets(): List<Asset> =
        assetDao.getAllVariableIncomeAssets().map { it.toDomain() }

    override suspend fun getByID(id: Long): Asset? =
        assetDao.find(id)?.toDomain()

    override suspend fun save(asset: Asset): Long =
        assetDao.save(asset.toEntity())
}