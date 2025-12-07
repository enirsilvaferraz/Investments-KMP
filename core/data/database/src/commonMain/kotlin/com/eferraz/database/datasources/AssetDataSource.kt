package com.eferraz.database.datasources

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset

public interface AssetDataSource {
    public suspend fun getAll(): List<Asset>
    public suspend fun getByID(id: Long): Asset?
    public suspend fun save(asset: FixedIncomeAsset): Long
    public suspend fun update(asset: FixedIncomeAsset)
    public suspend fun delete(id: Long)
}