package com.eferraz.usecases.repositories

import com.eferraz.entities.Asset

public interface AssetRepository {
    public fun getAll(): List<Asset>
}