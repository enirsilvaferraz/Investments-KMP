package com.eferraz.database.entities.goals

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela goal_investment_plans.
 * Representa o plano de investimento associado a uma meta financeira.
 */
@Entity(
    tableName = "goal_investment_plans",
    foreignKeys = [
        ForeignKey(
            entity = FinancialGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index(value = ["goalId"])
    ]
)
internal data class GoalInvestmentPlanEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "goalId")
    val goalId: Long,

    @ColumnInfo(name = "monthlyContribution")
    val monthlyContribution: Double,

    @ColumnInfo(name = "monthlyReturnRate")
    val monthlyReturnRate: Double,

    @ColumnInfo(name = "initialValue")
    val initialValue: Double = 0.0
)
