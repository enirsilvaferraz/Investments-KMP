package com.eferraz.entities

import com.eferraz.entities.liquidity.Liquidity

/**
 * Representa um ativo de renda variável.
 *
 * @property type O tipo de ativo de renda variável (ação, FII, etc.).
 * @property ticker O código de negociação único do ativo (ex: "PETR4").
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property liquidityDays O número de dias para resgate quando liquidity é D_PLUS_DAYS.
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
public data class VariableIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    public val type: VariableIncomeAssetType,
    public val ticker: String,
    public val liquidity: Liquidity,
    public val liquidityDays: Int,
    override val observations: String? = null
) : Asset
