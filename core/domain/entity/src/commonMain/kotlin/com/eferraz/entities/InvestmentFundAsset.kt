package com.eferraz.entities

import com.eferraz.entities.liquidity.OnDaysAfterSale
import kotlinx.datetime.LocalDate

/**
 * Representa um fundo de investimento.
 *
 * @property type A categoria do fundo de investimento (ações, multimercado, etc.).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property expirationDate Data de vencimento do título (opcional para fundos).
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
public data class InvestmentFundAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    public val type: InvestmentFundAssetType,
    public val liquidity: OnDaysAfterSale,
    public val expirationDate: LocalDate?,
    override val observations: String? = null
) : Asset
