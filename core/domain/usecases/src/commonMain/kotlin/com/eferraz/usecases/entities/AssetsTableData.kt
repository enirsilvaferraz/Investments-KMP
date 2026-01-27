package com.eferraz.usecases.entities

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import kotlinx.datetime.LocalDate

/**
 * Interface que representa uma linha da tabela de assets.
 * Cada implementação representa uma linha específica para um tipo de asset.
 */
public sealed interface AssetsTableData {
    /**
     * ID do asset (necessário para operações de atualização).
     */
    public val assetId: Long

    /**
     * Nome da corretora vinculada ao asset (String vazia se não houver vínculo).
     */
    public val brokerageName: String

    /**
     * ID da corretora vinculada (null se não houver vínculo).
     */
    public val brokerageId: Long?

    /**
     * Nome da meta financeira associada (String vazia se não houver meta).
     */
    public val goalName: String

    /**
     * ID da meta financeira vinculada (null se não houver meta).
     */
    public val goalId: Long?

    /**
     * Observações sobre o asset (String vazia se não houver observações).
     */
    public val observations: String
}

/**
 * Representa uma linha da tabela de Renda Fixa.
 */
public data class FixedIncomeAssetsTableData(
    override val assetId: Long,
    override val brokerageName: String,
    override val brokerageId: Long?,
    override val goalName: String,
    override val goalId: Long?,
    /**
     * Subcategoria do ativo de renda fixa.
     */
    public val subType: FixedIncomeSubType,
    /**
     * Tipo de cálculo de rendimento.
     */
    public val type: FixedIncomeAssetType,
    /**
     * Data de vencimento do título.
     */
    public val expirationDate: LocalDate,
    /**
     * Taxa contratada.
     */
    public val contractedYield: Double,
    /**
     * Rentabilidade relativa ao CDI (null se não aplicável).
     */
    public val cdiRelativeYield: Double?,
    /**
     * Nome do emissor.
     */
    public val issuerName: String,
    /**
     * ID do emissor (necessário para atualizações).
     */
    public val issuerId: Long,
    /**
     * Regra de liquidez do ativo.
     */
    public val liquidity: Liquidity,
    override val observations: String,
) : AssetsTableData

/**
 * Representa uma linha da tabela de Renda Variável.
 */
public data class VariableIncomeAssetsTableData(
    override val assetId: Long,
    override val brokerageName: String,
    override val brokerageId: Long?,
    override val goalName: String,
    override val goalId: Long?,
    /**
     * Tipo de ativo de renda variável.
     */
    public val type: VariableIncomeAssetType,
    /**
     * Ticker do ativo (ex: "PETR4").
     */
    public val ticker: String,
    /**
     * CNPJ formatado (String vazia se não houver CNPJ).
     */
    public val cnpj: String,
    /**
     * Nome do ativo.
     */
    public val name: String,
    /**
     * Nome do emissor.
     */
    public val issuerName: String,
    /**
     * ID do emissor (necessário para atualizações).
     */
    public val issuerId: Long,
    override val observations: String,
) : AssetsTableData

/**
 * Representa uma linha da tabela de Fundos de Investimento.
 */
public data class InvestmentFundAssetsTableData(
    override val assetId: Long,
    override val brokerageName: String,
    override val brokerageId: Long?,
    override val goalName: String,
    override val goalId: Long?,
    /**
     * Categoria do fundo de investimento.
     */
    public val type: InvestmentFundAssetType,
    /**
     * Nome do fundo.
     */
    public val name: String,
    /**
     * Regra de liquidez do ativo.
     */
    public val liquidity: Liquidity,
    /**
     * Número de dias para liquidação.
     */
    public val liquidityDays: Int,
    /**
     * Data de vencimento (null se não houver vencimento).
     */
    public val expirationDate: LocalDate?,
    /**
     * Nome do emissor.
     */
    public val issuerName: String,
    /**
     * ID do emissor (necessário para atualizações).
     */
    public val issuerId: Long,
    override val observations: String,
) : AssetsTableData

