package com.eferraz.database.mappers

import com.eferraz.database.entities.transaction.AssetTransactionEntity
import com.eferraz.database.entities.transaction.FixedIncomeTransactionEntity
import com.eferraz.database.entities.transaction.FundsTransactionEntity
import com.eferraz.database.entities.transaction.VariableIncomeTransactionEntity
import com.eferraz.database.entities.transaction.TransactionWithDetails
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.VariableIncomeTransaction

/**
 * Mappers para conversão entre entidades de domínio e entidades de banco de dados de transações.
 */

internal fun AssetTransaction.toEntity(): TransactionWithDetails {

    val baseEntity = AssetTransactionEntity(
        id = id,
        holdingId = holding.id,
        transactionDate = date,
        type = type,
        category = when (this) {
            is FixedIncomeTransaction -> "FIXED_INCOME"
            is VariableIncomeTransaction -> "VARIABLE_INCOME"
            is FundsTransaction -> "FUNDS"
        },
        observations = observations
    )

    return when (this) {

        is FixedIncomeTransaction -> TransactionWithDetails(
            transaction = baseEntity,
            fixedIncome = FixedIncomeTransactionEntity(
                transactionId = id,
                totalValue = totalValue
            )
        )

        is VariableIncomeTransaction -> TransactionWithDetails(
            transaction = baseEntity,
            variableIncome = VariableIncomeTransactionEntity(
                transactionId = id,
                quantity = quantity,
                unitPrice = unitPrice
            )
        )

        is FundsTransaction -> TransactionWithDetails(
            transaction = baseEntity,
            funds = FundsTransactionEntity(
                transactionId = id,
                totalValue = totalValue
            )
        )
    }
}

internal fun TransactionWithDetails.toDomain(holding: AssetHolding): AssetTransaction {

    val base = transaction

    return when {

        fixedIncome != null -> FixedIncomeTransaction(
            id = base.id,
            holding = holding,
            date = base.transactionDate,
            type = base.type,
            totalValue = fixedIncome.totalValue,
            observations = base.observations
        )

        variableIncome != null -> VariableIncomeTransaction(
            id = base.id,
            holding = holding,
            date = base.transactionDate,
            type = base.type,
            quantity = variableIncome.quantity,
            unitPrice = variableIncome.unitPrice,
            observations = base.observations
        )

        funds != null -> FundsTransaction(
            id = base.id,
            holding = holding,
            date = base.transactionDate,
            type = base.type,
            totalValue = funds.totalValue,
            observations = base.observations
        )

        else -> throw IllegalStateException("TransactionWithDetails must have at least one specific transaction type")
    }
}
