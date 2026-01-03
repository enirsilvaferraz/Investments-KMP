package com.eferraz.usecases.repositories

import com.eferraz.entities.Asset
import com.eferraz.entities.InvestmentCategory

public interface AssetRepository {
    public suspend fun getAll(): List<Asset>
    public suspend fun getByType(category: InvestmentCategory): List<Asset>
    public suspend fun getById(id: Long): Asset?
    public suspend fun getByTicker(ticker: String): Asset?
    public suspend fun save(asset: Asset): Long
}