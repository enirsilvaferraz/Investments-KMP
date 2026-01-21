package com.eferraz.usecases.repositories

import com.eferraz.entities.GoalInvestmentPlan

public interface GoalInvestmentPlanRepository {
    public suspend fun save(plan: GoalInvestmentPlan): Long
    public suspend fun getAll(): List<GoalInvestmentPlan>
    public suspend fun getById(id: Long): GoalInvestmentPlan?
    public suspend fun getByGoalId(goalId: Long): GoalInvestmentPlan?
    public suspend fun delete(id: Long)
    public suspend fun deleteByGoalId(goalId: Long)
}
