package com.eferraz.repositories

import com.eferraz.database.datasources.IssuerDataSource
import com.eferraz.usecases.repositories.IssuerRepository
import org.koin.core.annotation.Factory

@Factory(binds = [IssuerRepository::class])
internal class IssuerRepositoryImpl(
    private val dataSource: IssuerDataSource,
) : IssuerRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getByName(name: String) = dataSource.getByName(name)

    override suspend fun create(name: String) = dataSource.create(name)
}

