package com.eferraz.database.datasources

import com.eferraz.database.daos.GoalInvestmentPlanDao
import com.eferraz.database.entities.goals.GoalInvestmentPlanEntity
import com.eferraz.entities.GoalInvestmentPlan
import org.koin.core.annotation.Factory

@Factory(binds = [GoalInvestmentPlanDataSource::class])
internal class GoalInvestmentPlanDataSourceImpl(
    private val goalInvestmentPlanDao: GoalInvestmentPlanDao,
    private val financialGoalDataSource: FinancialGoalDataSource,
) : GoalInvestmentPlanDataSource {

    override suspend fun save(plan: GoalInvestmentPlan): Long {
        val entity = plan.toEntity()
        return goalInvestmentPlanDao.upsert(entity)
    }

    override suspend fun getAll(): List<GoalInvestmentPlan> {
        return goalInvestmentPlanDao.getAll().mapNotNull { it.toDomain() }
    }

    override suspend fun getById(id: Long): GoalInvestmentPlan? {
        return goalInvestmentPlanDao.getById(id)?.toDomain()
    }

    override suspend fun getByGoalId(goalId: Long): GoalInvestmentPlan? {
        return goalInvestmentPlanDao.getByGoalId(goalId)?.toDomain()
    }

    override suspend fun delete(id: Long) {
        goalInvestmentPlanDao.deleteById(id)
    }

    override suspend fun deleteByGoalId(goalId: Long) {
        goalInvestmentPlanDao.deleteByGoalId(goalId)
    }

    private suspend fun GoalInvestmentPlanEntity.toDomain(): GoalInvestmentPlan? {
        val goal = financialGoalDataSource.getById(goalId) ?: return null
        return GoalInvestmentPlan(
            id = id,
            goal = goal,
            monthlyContribution = monthlyContribution,
            monthlyReturnRate = monthlyReturnRate,
            initialValue = initialValue
        )
    }

    private fun GoalInvestmentPlan.toEntity() = GoalInvestmentPlanEntity(
        id = id ?: 0,
        goalId = goal.id,
        monthlyContribution = monthlyContribution,
        monthlyReturnRate = monthlyReturnRate,
        initialValue = initialValue
    )
}
