package com.eferraz.entities.assets

import kotlinx.datetime.LocalDate

/**
 * Representa um ativo de renda fixa. As suas propriedades definem o "contrato" do título.
 *
 * @property indexer O indexador de rentabilidade (pós-fixado, pré-fixado, etc.).
 * @property type O instrumento de renda fixa (CDB, LCI, etc.).
 * @property expirationDate Data de vencimento do título.
 * @property contractedYield Rentabilidade contratada no momento da aplicação.
 * @property cdiRelativeYield Rentabilidade relativa ao CDI (opcional).
 * @property liquidity A regra de liquidez que se aplica ao ativo.
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 * @property b3Identifier Identificador B3 para conciliação manual (opcional).
 * @property incomeTaxExempt Indica se o título é isento de IR (default `false` = "Não").
 */
public data class FixedIncomeAsset(
    override val id: Long = 0,
    override val issuer: Issuer,
    public val indexer: YieldIndexer,
    public val type: FixedIncomeAssetType,
    public val expirationDate: LocalDate,
    public val contractedYield: Double,
    public val cdiRelativeYield: Double? = null,
    public val liquidity: Liquidity,
    override val observations: String? = null,
    public val b3Identifier: String? = null,
    public val incomeTaxExempt: Boolean = false,
) : Asset {

    override val assetClass: AssetClass = AssetClass.FIXED_INCOME
}
