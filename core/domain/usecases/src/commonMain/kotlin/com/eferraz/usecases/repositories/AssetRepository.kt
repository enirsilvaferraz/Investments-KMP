package com.eferraz.usecases.repositories

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.InvestmentCategory

public interface AssetRepository: AppCrudRepository<Asset> {
    public suspend fun getByType(category: InvestmentCategory): List<Asset>
    public suspend fun getByTicker(ticker: String): Asset?
}