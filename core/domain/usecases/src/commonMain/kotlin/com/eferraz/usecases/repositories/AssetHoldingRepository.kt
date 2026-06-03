package com.eferraz.usecases.repositories

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.holdings.AssetHolding

public interface AssetHoldingRepository : AppCrudRepository<AssetHolding> {
    public suspend fun getByAssetId(assetId: Long): AssetHolding?
    public suspend fun getAllVariableIncomeAssets(): List<AssetHolding>
    public suspend fun getByAssetClass(assetClass: AssetClass): List<AssetHolding>
    public suspend fun getByGoalId(goalId: Long): List<AssetHolding>
}
