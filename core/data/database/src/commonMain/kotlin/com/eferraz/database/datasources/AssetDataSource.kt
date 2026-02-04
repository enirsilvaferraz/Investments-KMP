package com.eferraz.database.datasources

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.InvestmentCategory

public interface AssetDataSource {
    public suspend fun getAll(): List<Asset>
    public suspend fun getByType(category: InvestmentCategory): List<Asset>
    public suspend fun getAllVariableIncomeAssets(): List<Asset>
    public suspend fun getByID(id: Long): Asset?
    public suspend fun getByTicker(ticker: String): Asset?
    public suspend fun save(asset: Asset): Long
    public suspend fun delete(id: Long)
}