package com.eferraz.database.entities.goals

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.database.entities.supports.OwnerEntity
import kotlinx.datetime.LocalDate

/**
 * Entidade Room para a tabela financial_goals.
 * Representa uma meta financeira a ser alcançada.
 */
@Entity(
    tableName = "financial_goals",
    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.Companion.RESTRICT
        )
    ],
    indices = [
        Index(value = ["ownerId"])
    ]
)
internal data class FinancialGoalEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "ownerId")
    val ownerId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "targetValue")
    val targetValue: Double,

    @ColumnInfo(name = "startDate")
    val startDate: LocalDate,

    @ColumnInfo(name = "description")
    val description: String? = null
)
