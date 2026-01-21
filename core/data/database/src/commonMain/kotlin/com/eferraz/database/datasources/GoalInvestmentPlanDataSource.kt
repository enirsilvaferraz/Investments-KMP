package com.eferraz.database.datasources

import com.eferraz.entities.GoalInvestmentPlan

public interface GoalInvestmentPlanDataSource {
    public suspend fun save(plan: GoalInvestmentPlan): Long
    public suspend fun getAll(): List<GoalInvestmentPlan>
    public suspend fun getById(id: Long): GoalInvestmentPlan?
    public suspend fun getByGoalId(goalId: Long): GoalInvestmentPlan?
    public suspend fun delete(id: Long)
    public suspend fun deleteByGoalId(goalId: Long)
}
