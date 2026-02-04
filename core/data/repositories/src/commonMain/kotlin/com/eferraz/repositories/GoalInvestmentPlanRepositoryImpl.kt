package com.eferraz.repositories

import com.eferraz.database.datasources.GoalInvestmentPlanDataSource
import com.eferraz.entities.goals.GoalInvestmentPlan
import com.eferraz.usecases.repositories.GoalInvestmentPlanRepository
import org.koin.core.annotation.Factory

@Factory(binds = [GoalInvestmentPlanRepository::class])
internal class GoalInvestmentPlanRepositoryImpl(
    private val dataSource: GoalInvestmentPlanDataSource,
) : GoalInvestmentPlanRepository {

    override suspend fun upsert(plan: GoalInvestmentPlan): Long = dataSource.save(plan)

    override suspend fun getAll() = dataSource.getAll()

    override suspend fun getById(id: Long) = dataSource.getById(id)

    override suspend fun getByGoal(goalId: Long) = dataSource.getByGoalId(goalId)

    override suspend fun delete(id: Long) = dataSource.delete(id)

    override suspend fun deleteByGoalId(goalId: Long) = dataSource.deleteByGoalId(goalId)
}
