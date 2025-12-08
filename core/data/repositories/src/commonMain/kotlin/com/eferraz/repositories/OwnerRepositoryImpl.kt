package com.eferraz.repositories

import com.eferraz.database.datasources.OwnerDataSource
import com.eferraz.usecases.repositories.OwnerRepository
import org.koin.core.annotation.Factory

@Factory(binds = [OwnerRepository::class])
internal class OwnerRepositoryImpl(
    private val dataSource: OwnerDataSource,
) : OwnerRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getFirst() = dataSource.getFirst()
}

