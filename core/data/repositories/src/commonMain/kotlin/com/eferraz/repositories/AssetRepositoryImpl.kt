package com.eferraz.repositories

import com.eferraz.entities.Asset
import com.eferraz.repositories.datasources.AssetInMemoryDataSource
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl : AssetRepository {
    override fun getAll(): List<Asset> {
        return AssetInMemoryDataSource.assets
    }
}