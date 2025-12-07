package com.eferraz.usecases.repositories

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset

public interface AssetRepository {
    public suspend fun getAll(): List<Asset>
    public suspend fun getById(id: Long): Asset?
    public suspend fun save(asset: FixedIncomeAsset): Long
    public suspend fun update(asset: FixedIncomeAsset)
    public suspend fun delete(id: Long)
}