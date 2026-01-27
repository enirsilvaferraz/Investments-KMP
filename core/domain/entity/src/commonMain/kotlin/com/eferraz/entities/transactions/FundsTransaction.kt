package com.eferraz.entities.transactions

import com.eferraz.entities.holdings.AssetHolding
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
    override val totalValue: Double,
    override val observations: String? = null
) : AssetTransaction
