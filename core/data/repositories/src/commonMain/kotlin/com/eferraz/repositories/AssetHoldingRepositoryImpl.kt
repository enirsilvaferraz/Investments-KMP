package com.eferraz.repositories

import com.eferraz.database.datasources.AssetHoldingDataSource
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.usecases.repositories.AssetHoldingRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetHoldingRepository::class])
internal class AssetHoldingRepositoryImpl(
    private val dataSource: AssetHoldingDataSource,
) : AssetHoldingRepository {

    override suspend fun upsert(model: AssetHolding) = dataSource.save(model)

    override suspend fun getById(id: Long): AssetHolding = dataSource.getById(id)

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getByAssetId(assetId: Long) = dataSource.getByAssetId(assetId)

    override suspend fun getAllVariableIncomeAssets() = dataSource.getAllVariableIncomeAssets()

    override suspend fun getByCategory(category: InvestmentCategory) = dataSource.getByCategory(category)

    override suspend fun getByGoalId(goalId: Long) = dataSource.getByGoalId(goalId)

    override suspend fun delete(id: Long) = dataSource.delete(id)
}

