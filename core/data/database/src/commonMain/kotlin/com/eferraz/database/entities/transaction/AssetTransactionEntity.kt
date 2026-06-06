package com.eferraz.database.entities.transaction

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.entities.transactions.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Entidade Room para a tabela achatada asset_transactions.
 */
@Entity(
    tableName = "asset_transactions",
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
        Index(value = ["transactionDate"]),
        Index(value = ["type"]),
        Index(value = ["holdingId", "transactionDate"])
    ]
)
internal data class AssetTransactionEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "holdingId")
    val holdingId: Long,

    @ColumnInfo(name = "transactionDate")
    val transactionDate: LocalDate,

    @ColumnInfo(name = "type")
    val type: TransactionType,

    @ColumnInfo(name = "quantity", defaultValue = "1")
    val quantity: Double,

    @ColumnInfo(name = "unitPrice", defaultValue = "0")
    val unitPrice: Double,
)
