package com.eferraz.database.entities.histories

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eferraz.database.entities.holdings.AssetHoldingEntity
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
            onDelete = ForeignKey.Companion.CASCADE
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
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "holdingId")
    val holdingId: Long,
    @ColumnInfo(name = "referenceDate")
    val referenceDate: YearMonth,
    @ColumnInfo(name = "endOfMonthValue")
    val endOfMonthValue: Double,
    @ColumnInfo(name = "endOfMonthQuantity")
    val endOfMonthQuantity: Double,
    @ColumnInfo(name = "endOfMonthAverageCost")
    val endOfMonthAverageCost: Double,
    @ColumnInfo(name = "totalInvested")
    val totalInvested: Double
)
