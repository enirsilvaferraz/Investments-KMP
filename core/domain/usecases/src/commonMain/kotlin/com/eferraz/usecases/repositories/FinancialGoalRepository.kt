package com.eferraz.usecases.repositories

import com.eferraz.entities.goals.FinancialGoal

public interface FinancialGoalRepository : AppCrudRepository<FinancialGoal> {
    public suspend fun getByName(name: String): FinancialGoal?
    public suspend fun getByOwnerId(ownerId: Long): List<FinancialGoal>
}
