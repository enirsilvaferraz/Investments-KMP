package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.eferraz.database.entities.goals.FinancialGoalEntity

/**
 * DAO para operações CRUD na tabela financial_goals.
 */
@Dao
internal interface FinancialGoalDao {

    @Upsert
    suspend fun upsert(goal: FinancialGoalEntity): Long

    @Insert
    suspend fun insertAll(goals: List<FinancialGoalEntity>): List<Long>

    @Query("SELECT * FROM financial_goals")
    suspend fun getAll(): List<FinancialGoalEntity>

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getById(id: Long): FinancialGoalEntity?

    @Query("SELECT * FROM financial_goals WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): FinancialGoalEntity?

    @Query("SELECT * FROM financial_goals WHERE ownerId = :ownerId")
    suspend fun getByOwnerId(ownerId: Long): List<FinancialGoalEntity>

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
