package com.eferraz.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela fixed_income_transactions.
 * Representa os atributos específicos de transações de renda fixa.
 * Relacionamento 1-1 com AssetTransactionEntity.
 */
@Entity(
    tableName = "fixed_income_transactions",
    foreignKeys = [
        ForeignKey(
            entity = AssetTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class FixedIncomeTransactionEntity(

    @PrimaryKey
    @ColumnInfo(name = "transactionId")
    val transactionId: Long,

    @ColumnInfo(name = "totalValue")
    val totalValue: Double
)
