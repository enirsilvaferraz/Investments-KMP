package com.eferraz.database.entities.transaction

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class intermediária que representa uma transação completa com seus detalhes específicos.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class TransactionWithDetails(

    @Embedded
    val transaction: AssetTransactionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val fixedIncome: FixedIncomeTransactionEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val variableIncome: VariableIncomeTransactionEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val funds: FundsTransactionEntity? = null,
)