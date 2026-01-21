package com.eferraz.usecases.repositories

import com.eferraz.entities.FinancialGoal

public interface FinancialGoalRepository {
    public suspend fun save(goal: FinancialGoal): Long
    public suspend fun getAll(): List<FinancialGoal>
    public suspend fun getById(id: Long): FinancialGoal?
    public suspend fun getByOwnerId(ownerId: Long): List<FinancialGoal>
    public suspend fun delete(id: Long)
}
