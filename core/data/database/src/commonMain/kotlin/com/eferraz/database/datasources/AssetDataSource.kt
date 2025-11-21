package com.eferraz.database.datasources

import com.eferraz.entities.Asset
import kotlinx.coroutines.flow.Flow

public interface AssetDataSource {
    public fun getAll(): Flow<List<Asset>>
    public fun getByID(id: Long): Flow<Asset>
}