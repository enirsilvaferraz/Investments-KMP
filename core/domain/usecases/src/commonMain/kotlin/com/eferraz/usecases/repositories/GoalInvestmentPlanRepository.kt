package com.eferraz.usecases.repositories

import com.eferraz.entities.goals.GoalInvestmentPlan

public interface GoalInvestmentPlanRepository: AppCrudRepository<GoalInvestmentPlan> {
    public suspend fun getByGoal(goalId: Long): GoalInvestmentPlan?
    public suspend fun deleteByGoalId(goalId: Long)
}
