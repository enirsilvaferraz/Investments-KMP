package com.eferraz.database.entities.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela funds_transactions.
 * Representa os atributos específicos de transações de fundos de investimento.
 * Relacionamento 1-1 com AssetTransactionEntity.
 */
@Entity(
    tableName = "funds_transactions",
    foreignKeys = [
        ForeignKey(
            entity = AssetTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class FundsTransactionEntity(

    @PrimaryKey
    @ColumnInfo(name = "transactionId")
    val transactionId: Long,

    @ColumnInfo(name = "totalValue")
    val totalValue: Double,

    ) : BaseTransactionEntity
