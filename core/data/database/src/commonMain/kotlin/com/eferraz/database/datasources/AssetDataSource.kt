package com.eferraz.database.datasources

import com.eferraz.entities.Asset

public interface AssetDataSource {
    public suspend fun getAll(): List<Asset>
    public suspend fun getByID(id: Long): Asset
}