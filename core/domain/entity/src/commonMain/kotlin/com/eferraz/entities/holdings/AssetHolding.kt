package com.eferraz.entities.holdings

import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Owner
import com.eferraz.entities.assets.Asset

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
 * @property goal A meta financeira à qual esta posição contribui (opcional).
 *
 */
public data class AssetHolding(
    public val id: Long,
    public val asset: Asset,
    public val owner: Owner,
    public val brokerage: Brokerage,
    public val goal: FinancialGoal? = null,
)
