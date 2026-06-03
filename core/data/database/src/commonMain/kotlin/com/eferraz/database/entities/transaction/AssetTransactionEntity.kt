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
 * Entidade Room para a tabela base asset_transactions.
 * Representa os campos comuns a todas as transações de ativos.
 *
 * @property assetClass Discriminador: 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND'
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
        Index(value = ["asset_class"]),
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

    @ColumnInfo(name = "asset_class")
    val assetClass: String, // 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND' // TODO Transformar em enum Category

    @ColumnInfo(name = "observations")
    val observations: String? = null
)
