package com.eferraz.database.entities.histories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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