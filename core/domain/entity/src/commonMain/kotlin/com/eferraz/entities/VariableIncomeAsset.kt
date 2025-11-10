package com.eferraz.entities

import com.eferraz.entities.liquidity.OnDaysAfterSale

/**
 * Representa um ativo de renda variável.
 *
 * @property type O tipo de ativo de renda variável (ação, FII, etc.).
 * @property ticker O código de negociação único do ativo (ex: "PETR4").
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 */
public data class VariableIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    public val type: VariableIncomeAssetType,
    public val ticker: String,
    public val liquidity: OnDaysAfterSale
) : Asset
