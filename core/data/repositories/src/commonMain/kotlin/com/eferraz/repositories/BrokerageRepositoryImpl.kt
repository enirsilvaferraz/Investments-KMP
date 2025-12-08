package com.eferraz.repositories

import com.eferraz.database.datasources.BrokerageDataSource
import com.eferraz.usecases.repositories.BrokerageRepository
import org.koin.core.annotation.Factory

@Factory(binds = [BrokerageRepository::class])
internal class BrokerageRepositoryImpl(
    private val dataSource: BrokerageDataSource,
) : BrokerageRepository {

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getByName(name: String) = dataSource.getByName(name)
}

