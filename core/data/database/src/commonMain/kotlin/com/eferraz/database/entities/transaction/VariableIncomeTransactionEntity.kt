package com.eferraz.database.entities.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela variable_income_transactions.
 * Representa os atributos específicos de transações de renda variável.
 * Relacionamento 1-1 com AssetTransactionEntity.
 */
@Entity(
    tableName = "variable_income_transactions",
    foreignKeys = [
        ForeignKey(
            entity = AssetTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class VariableIncomeTransactionEntity(

    @PrimaryKey
    @ColumnInfo(name = "transactionId")
    val transactionId: Long,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "unitPrice")
    val unitPrice: Double,

    ) : BaseTransactionEntity
