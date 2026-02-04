package com.eferraz.usecases.repositories

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.assets.InvestmentCategory

public interface AssetHoldingRepository: AppCrudRepository<AssetHolding> {
    public suspend fun getByAssetId(assetId: Long): AssetHolding?
    public suspend fun getAllVariableIncomeAssets(): List<AssetHolding>
    public suspend fun getByCategory(category: InvestmentCategory): List<AssetHolding>
    public suspend fun getByGoalId(goalId: Long): List<AssetHolding>
}

