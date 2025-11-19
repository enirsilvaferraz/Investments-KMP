package com.eferraz.repositories

import com.eferraz.repositories.datasources.AssetInMemoryDataSource
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetInMemoryDataSource,
) : AssetRepository {

    override fun getAll() = dataSource.getAll()
}