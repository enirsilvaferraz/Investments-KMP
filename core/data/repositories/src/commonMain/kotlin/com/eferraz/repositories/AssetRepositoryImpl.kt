package com.eferraz.repositories

import com.eferraz.database.datasources.AssetDataSource
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetRepository::class])
internal class AssetRepositoryImpl(
    private val dataSource: AssetDataSource,
) : AssetRepository {

    override fun getAll() = dataSource.getAll()
}