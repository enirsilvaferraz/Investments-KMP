package com.eferraz.usecases.repositories

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass

public interface AssetRepository : AppCrudRepository<Asset> {
    public suspend fun getByAssetClass(assetClass: AssetClass): List<Asset>
    public suspend fun getByTicker(ticker: String): Asset?
}
