package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Transações de Renda Fixa (CDB, LCI, LCA, Poupança, etc.).
 * Regra: Apenas valor total (não há quantidade unitária).
 */
public data class FixedIncomeTransaction(
    override val id: Long,
    override val holding: AssetHolding,
    override val date: LocalDate,
    override val type: TransactionType,
    public val totalValue: Double, // O valor total da transação.
    override val observations: String? = null
) : AssetTransaction
