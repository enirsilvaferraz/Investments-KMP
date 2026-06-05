package com.eferraz.entities.assets

import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity

/**
 * Representa um fundo de investimento.
 *
 * @property type A categoria do fundo de investimento (ações, multimercado, etc.).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
public data class InvestmentFundAsset(
    override val id: Long = 0,
    public val name: String,
    override val issuer: Issuer,
    public val type: InvestmentFundAssetType,
    public val liquidity: Liquidity,
    override val observations: String? = null
) : Asset {

    override val assetClass: AssetClass = AssetClass.INVESTMENT_FUND
}
