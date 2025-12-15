package com.eferraz.database.entities.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.entities.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Entidade Room para a tabela base asset_transactions.
 * Representa os campos comuns a todas as transações de ativos.
 *
 * @property category Discriminador: 'FIXED_INCOME', 'VARIABLE_INCOME', 'FUNDS'
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
        Index(value = ["category"]),
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

    @ColumnInfo(name = "category")
    val category: String, // 'FIXED_INCOME', 'VARIABLE_INCOME', 'FUNDS'

    @ColumnInfo(name = "observations")
    val observations: String? = null
)