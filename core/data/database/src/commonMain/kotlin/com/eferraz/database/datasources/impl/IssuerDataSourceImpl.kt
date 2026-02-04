package com.eferraz.database.datasources.impl

import com.eferraz.database.daos.IssuerDao
import com.eferraz.database.datasources.IssuerDataSource
import com.eferraz.database.entities.supports.IssuerEntity
import com.eferraz.entities.assets.Issuer
import org.koin.core.annotation.Factory

@Factory(binds = [IssuerDataSource::class])
internal class IssuerDataSourceImpl(
    private val issuerDao: IssuerDao,
) : IssuerDataSource {

    override suspend fun getAll(): List<Issuer> {
        return issuerDao.getAll().map { it.toModel() }
    }

    override suspend fun getByName(name: String): Issuer? {
        return issuerDao.getByName(name)?.toModel()
    }

    override suspend fun create(name: String): Issuer {
        val entity = IssuerEntity(name = name.trim())
        val id = issuerDao.insert(entity)
        return Issuer(id = id, name = name.trim())
    }

    private fun IssuerEntity.toModel() = Issuer(
        id = id,
        name = name,
        isInLiquidation = isInLiquidation
    )
}

