package com.eferraz.database.mappers

import com.eferraz.database.entities.transaction.AssetTransactionEntity
import com.eferraz.entities.transactions.AssetTransaction

internal fun AssetTransaction.toEntity(holdingId: Long): AssetTransactionEntity =
    AssetTransactionEntity(
        id = id,
        holdingId = holdingId,
        transactionDate = date,
        type = type,
        quantity = quantity,
        unitPrice = unitPrice,
        allocatedFee = allocatedFee,
    )

internal fun AssetTransactionEntity.toDomain(): AssetTransaction =
    AssetTransaction(
        id = id,
        date = transactionDate,
        type = type,
        quantity = quantity,
        unitPrice = unitPrice,
        allocatedFee = allocatedFee,
    )
