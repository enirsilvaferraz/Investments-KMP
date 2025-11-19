package com.eferraz.usecases.repositories

import com.eferraz.entities.Asset
import kotlinx.coroutines.flow.Flow

public interface AssetRepository {
    public fun getAll(): Flow<List<Asset>>
}