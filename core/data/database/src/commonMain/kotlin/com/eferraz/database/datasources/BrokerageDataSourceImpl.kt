package com.eferraz.database.datasources

import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.entities.BrokerageEntity
import com.eferraz.entities.Brokerage
import org.koin.core.annotation.Factory

@Factory(binds = [BrokerageDataSource::class])
internal class BrokerageDataSourceImpl(
    private val brokerageDao: BrokerageDao,
) : BrokerageDataSource {

    override suspend fun getAll(): List<Brokerage> {
        return brokerageDao.getAll().map { it.toModel() }
    }

    override suspend fun getByName(name: String): Brokerage? {
        return brokerageDao.getAll()
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.toModel()
    }

    private fun BrokerageEntity.toModel() = Brokerage(
        id = id,
        name = name
    )
}

