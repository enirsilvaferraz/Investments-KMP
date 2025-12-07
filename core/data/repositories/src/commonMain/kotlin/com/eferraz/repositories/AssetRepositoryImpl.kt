package com.eferraz.repositories

import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetDataSource,
) : AssetRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getById(id: Long) = dataSource.getByID(id)

    override suspend fun save(asset: com.eferraz.entities.FixedIncomeAsset) = dataSource.save(asset)

    override suspend fun update(asset: com.eferraz.entities.FixedIncomeAsset) = dataSource.update(asset)

    override suspend fun delete(id: Long) = dataSource.delete(id)
}