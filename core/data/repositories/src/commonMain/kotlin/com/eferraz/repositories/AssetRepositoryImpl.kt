package com.eferraz.repositories

import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetDataSource,
) : AssetRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getByType(category: InvestmentCategory) = dataSource.getByType(category)

    override suspend fun getById(id: Long) = dataSource.getByID(id)

    override suspend fun getByTicker(ticker: String) = dataSource.getByTicker(ticker)

    override suspend fun save(asset: Asset) = dataSource.save(asset)
}