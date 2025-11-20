package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.YearMonth

/**
 * Entidade Room para a tabela holding_history.
 * Representa um snapshot mensal de uma AssetHolding.
 */
@Entity(
    tableName = "holding_history",
    foreignKeys = [
        ForeignKey(
            entity = AssetHoldingEntity::class,
            parentColumns = ["id"],
            childColumns = ["holdingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["holdingId"]),
        Index(value = ["referenceDate"]),
        Index(value = ["holdingId", "referenceDate"], unique = true)
    ]
)
internal data class HoldingHistoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val holdingId: Long,
    val referenceDate: YearMonth,
    val endOfMonthValue: Double,
    val endOfMonthQuantity: Double,
    val endOfMonthAverageCost: Double,
    val totalInvested: Double
)

