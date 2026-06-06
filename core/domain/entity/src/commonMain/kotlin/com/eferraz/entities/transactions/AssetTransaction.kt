package com.eferraz.entities.transactions

import kotlinx.datetime.LocalDate

/**
 * Transação de ativo unificada para todas as classes (RF, RV, fundos).
 * O valor total é sempre derivado de [quantity] × [unitPrice].
 */
public data class AssetTransaction(
    public val id: Long,
    public val date: LocalDate,
    public val type: TransactionType,
    public val quantity: Double,
    public val unitPrice: Double,
) {
    public val totalValue: Double get() = quantity * unitPrice
}
