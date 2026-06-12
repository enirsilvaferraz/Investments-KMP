package com.eferraz.entities.transactions

import kotlinx.datetime.LocalDate

/**
 * Transação de ativo unificada para todas as classes (RF, RV, fundos).
 * [grossValue] é derivado de [quantity] × [unitPrice]; [netValue] inclui [allocatedFee].
 */
public data class AssetTransaction(
    public val id: Long,
    public val date: LocalDate,
    public val type: TransactionType,
    public val quantity: Double,
    public val unitPrice: Double,
    public val allocatedFee: Double = 0.0,
) {
    public val grossValue: Double get() = quantity * unitPrice

    public val netValue: Double get() = when (type) {
        TransactionType.PURCHASE -> grossValue + allocatedFee
        TransactionType.SALE -> grossValue - allocatedFee
    }
}
