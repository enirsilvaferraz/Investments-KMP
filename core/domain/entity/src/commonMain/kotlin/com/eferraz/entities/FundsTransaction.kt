package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Transações de Fundos de Investimento.
 * Regra: Apenas valor total (não há quantidade unitária).
 */
public data class FundsTransaction(
    override val id: Long,
    override val holding: AssetHolding,
    override val date: LocalDate,
    override val type: TransactionType,
    public val totalValue: Double, // O valor total da transação.
    override val observations: String? = null
) : AssetTransaction
