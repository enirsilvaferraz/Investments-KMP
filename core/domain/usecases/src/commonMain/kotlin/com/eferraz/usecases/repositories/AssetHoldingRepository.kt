package com.eferraz.usecases.repositories

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.InvestmentCategory

public interface AssetHoldingRepository {
    public suspend fun save(assetHolding: AssetHolding): Long
    public suspend fun getByAssetId(assetId: Long): AssetHolding?
    public suspend fun getAll(): List<AssetHolding>
    public suspend fun getAllVariableIncomeAssets(): List<AssetHolding>
    public suspend fun getByCategory(category: InvestmentCategory): List<AssetHolding>
    public suspend fun delete(id: Long)
}

