package com.eferraz.database.datasources.impl

import com.eferraz.database.daos.FinancialGoalDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.datasources.FinancialGoalDataSource
import com.eferraz.database.entities.goals.FinancialGoalEntity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Owner
import org.koin.core.annotation.Factory

@Factory(binds = [FinancialGoalDataSource::class])
internal class FinancialGoalDataSourceImpl(
    private val financialGoalDao: FinancialGoalDao,
    private val ownerDao: OwnerDao,
) : FinancialGoalDataSource {

    override suspend fun save(goal: FinancialGoal): Long {
        val entity = goal.toEntity()
        return financialGoalDao.upsert(entity)
    }

    override suspend fun getAll(): List<FinancialGoal> {
        return financialGoalDao.getAll().mapNotNull { it.toDomain() }
    }

    override suspend fun getById(id: Long): FinancialGoal? {
        return financialGoalDao.getById(id)?.toDomain()
    }

    override suspend fun getByName(name: String): FinancialGoal? {
        return financialGoalDao.getByName(name)?.toDomain()
    }

    override suspend fun getByOwnerId(ownerId: Long): List<FinancialGoal> {
        return financialGoalDao.getByOwnerId(ownerId).mapNotNull { it.toDomain() }
    }

    override suspend fun delete(id: Long) {
        financialGoalDao.deleteById(id)
    }

    private suspend fun FinancialGoalEntity.toDomain(): FinancialGoal? {
        val ownerEntity = ownerDao.getById(ownerId) ?: return null
        return FinancialGoal(
            id = id,
            owner = Owner(id = ownerEntity.id, name = ownerEntity.name),
            name = name,
            targetValue = targetValue,
            startDate = startDate,
            description = description
        )
    }

    private fun FinancialGoal.toEntity() = FinancialGoalEntity(
        id = id,
        ownerId = owner.id,
        name = name,
        targetValue = targetValue,
        startDate = startDate,
        description = description
    )
}
