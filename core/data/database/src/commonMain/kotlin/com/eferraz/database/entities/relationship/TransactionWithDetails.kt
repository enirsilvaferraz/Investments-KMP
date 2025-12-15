package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetTransactionEntity
import com.eferraz.database.entities.FixedIncomeTransactionEntity
import com.eferraz.database.entities.FundsTransactionEntity
import com.eferraz.database.entities.VariableIncomeTransactionEntity

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
    val fixedIncome: FixedIncomeTransactionEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val variableIncome: VariableIncomeTransactionEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val funds: FundsTransactionEntity?
)
