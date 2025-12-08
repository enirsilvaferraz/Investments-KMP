package com.eferraz.repositories

import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetDataSource,
) : AssetRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getById(id: Long) = dataSource.getByID(id)

    override suspend fun save(asset: FixedIncomeAsset) = dataSource.save(asset)

    override suspend fun save(asset: InvestmentFundAsset) = dataSource.save(asset)

    override suspend fun save(asset: VariableIncomeAsset) = dataSource.save(asset)
}