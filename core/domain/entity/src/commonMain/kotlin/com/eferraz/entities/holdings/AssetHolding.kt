package com.eferraz.entities.holdings

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Owner
import com.eferraz.entities.transactions.AssetTransaction

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
 * @property goal A meta financeira à qual esta posição contribui (opcional).
 * @property transactions Movimentações da posição (lista completa, ordenada na hidratação).
 *
 */
public data class AssetHolding(
    public val id: Long,
    public val asset: Asset,
    public val owner: Owner,
    public val brokerage: Brokerage,
    public val goal: FinancialGoal? = null,
    public val transactions: List<AssetTransaction> = emptyList(),
)
