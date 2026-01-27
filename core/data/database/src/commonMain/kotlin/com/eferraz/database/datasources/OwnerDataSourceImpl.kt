package com.eferraz.database.datasources

import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.supports.OwnerEntity
import com.eferraz.entities.holdings.Owner
import org.koin.core.annotation.Factory

@Factory(binds = [OwnerDataSource::class])
internal class OwnerDataSourceImpl(
    private val ownerDao: OwnerDao,
) : OwnerDataSource {

    override suspend fun getAll(): List<Owner> {
        return ownerDao.getAll().map { it.toModel() }
    }

    override suspend fun getFirst(): Owner? {
        return ownerDao.getAll().firstOrNull()?.toModel()
    }

    private fun OwnerEntity.toModel() = Owner(
        id = id,
        name = name
    )
}

