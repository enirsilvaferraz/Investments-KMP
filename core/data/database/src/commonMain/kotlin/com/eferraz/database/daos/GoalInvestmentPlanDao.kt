package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.eferraz.database.entities.goals.GoalInvestmentPlanEntity

/**
 * DAO para operações CRUD na tabela goal_investment_plans.
 */
@Dao
internal interface GoalInvestmentPlanDao {

    @Upsert
    suspend fun upsert(plan: GoalInvestmentPlanEntity): Long

    @Insert
    suspend fun insertAll(plans: List<GoalInvestmentPlanEntity>): List<Long>

    @Query("SELECT * FROM goal_investment_plans")
    suspend fun getAll(): List<GoalInvestmentPlanEntity>

    @Query("SELECT * FROM goal_investment_plans WHERE id = :id")
    suspend fun getById(id: Long): GoalInvestmentPlanEntity?

    @Query("SELECT * FROM goal_investment_plans WHERE goalId = :goalId")
    suspend fun getByGoalId(goalId: Long): GoalInvestmentPlanEntity?

    @Query("DELETE FROM goal_investment_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goal_investment_plans WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: Long)
}
