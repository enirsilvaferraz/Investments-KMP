package com.eferraz.repositories

import com.eferraz.database.datasources.FinancialGoalDataSource
import com.eferraz.usecases.repositories.FinancialGoalRepository
import org.koin.core.annotation.Factory

@Factory(binds = [FinancialGoalRepository::class])
internal class FinancialGoalRepositoryImpl(
    private val dataSource: FinancialGoalDataSource,
) : FinancialGoalRepository {

    override suspend fun save(goal: com.eferraz.entities.FinancialGoal): Long = dataSource.save(goal)

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getById(id: Long) = dataSource.getById(id)

    override suspend fun getByName(name: String) = dataSource.getByName(name)

    override suspend fun getByOwnerId(ownerId: Long) = dataSource.getByOwnerId(ownerId)

    override suspend fun delete(id: Long) = dataSource.delete(id)
}
