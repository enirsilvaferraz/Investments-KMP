package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Transações de Renda Variável (Ações, FIIs, ETFs).
 * Regra: Quantidade e preço unitário (calcula valor total automaticamente).
 */
public data class VariableIncomeTransaction(
    override val id: Long,
    override val holding: AssetHolding,
    override val date: LocalDate,
    override val type: TransactionType,
    public val quantity: Double, // A quantidade de unidades transacionadas.
    public val unitPrice: Double, // O preço unitário da transação.
    override val observations: String? = null,
) : AssetTransaction {

    /**
     * O valor total da transação, calculado como quantity * unitPrice.
     */
    public val totalValue: Double
        get() = quantity * unitPrice
}
