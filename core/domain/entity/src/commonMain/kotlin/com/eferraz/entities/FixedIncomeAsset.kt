package com.eferraz.entities

import com.eferraz.entities.liquidity.FixedLiquidity
import kotlinx.datetime.LocalDate

/**
 * Representa um ativo de renda fixa. As suas propriedades definem o "contrato" do título.
 *
 * @property type O tipo de cálculo de rendimento (pós-fixado, pré-fixado, etc.).
 * @property subType O instrumento de renda fixa (CDB, LCI, etc.).
 * @property expirationDate Data de vencimento do título.
 * @property contractedYield Rentabilidade contratada no momento da aplicação.
 * @property cdiRelativeYield Rentabilidade relativa ao CDI (opcional).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 */
public data class FixedIncomeAsset(
    override val id: Long,
    override val name: String,
    override val issuer: Issuer,
    public val type: FixedIncomeAssetType,
    public val subType: FixedIncomeSubType,
    public val expirationDate: LocalDate,
    public val contractedYield: Double,
    public val cdiRelativeYield: Double?,
    public val liquidity: FixedLiquidity
) : Asset
