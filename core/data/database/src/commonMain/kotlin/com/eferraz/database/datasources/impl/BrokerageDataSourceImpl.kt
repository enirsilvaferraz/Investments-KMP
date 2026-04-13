package com.eferraz.database.datasources.impl

import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.datasources.BrokerageDataSource
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.entities.holdings.Brokerage
import org.koin.core.annotation.Factory

@Factory(binds = [BrokerageDataSource::class])
internal class BrokerageDataSourceImpl(
    private val brokerageDao: BrokerageDao,
) : BrokerageDataSource {

    override suspend fun getAll(): List<Brokerage> =
        brokerageDao.getAll().map { it.toModel() }

    override suspend fun getByName(name: String): Brokerage? =
        brokerageDao.getAll()
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.toModel()

    override suspend fun getById(id: Long): Brokerage? =
        brokerageDao.getById(id)?.toModel()

    private fun BrokerageEntity.toModel() =
        Brokerage(
            id = id,
            name = name
        )
}
