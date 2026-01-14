package com.eferraz.usecases.entities

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAssetType
import kotlinx.datetime.LocalDate

/**
 * Interface que representa uma linha da tabela de histórico de posições.
 * Cada implementação representa uma linha específica para um tipo de asset.
 */
public sealed interface HistoryTableData {

    /**
     * ID da entrada de histórico atual (necessário para atualizações).
     */
    public val currentEntry: HoldingHistoryEntry

    /**
     * Nome da corretora.
     */
    public val brokerageName: String

    /**
     * Nome do emissor.
     */
    public val issuerName: String

    /**
     * Observações sobre o asset (String vazia se não houver observações).
     */
    public val observations: String

    /**
     * Valor anterior (valor do mês anterior).
     */
    public val previousValue: Double

    /**
     * Valor atual (valor do mês de referência).
     */
    public val currentValue: Double

    /**
     * Percentual de apreciação.
     */
    public val appreciation: Double

    /**
     * Indica se o valor pode ser editado.
     */
    public val editable: Boolean

    /**
     * Total de contribuições (aportes) no período.
     */
    public val totalContributions: Double

    /**
     * Total de retiradas no período.
     */
    public val totalWithdrawals: Double
}

/**
 * Representa uma linha da tabela de histórico de Renda Fixa.
 */
public data class FixedIncomeHistoryTableData(
    override val currentEntry: HoldingHistoryEntry,
    override val brokerageName: String,
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
    override val issuerName: String,
    /**
     * Regra de liquidez do ativo.
     */
    public val liquidity: Liquidity,
    override val observations: String,
    override val previousValue: Double,
    override val currentValue: Double,
    override val appreciation: Double,
    override val editable: Boolean,
    override val totalContributions: Double,
    override val totalWithdrawals: Double,
) : HistoryTableData

/**
 * Representa uma linha da tabela de histórico de Renda Variável.
 */
public data class VariableIncomeHistoryTableData(
    override val currentEntry: HoldingHistoryEntry,
    override val brokerageName: String,
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
    override val issuerName: String,
    override val observations: String,
    override val previousValue: Double,
    override val currentValue: Double,
    override val appreciation: Double,
    override val editable: Boolean,
    override val totalContributions: Double,
    override val totalWithdrawals: Double,
) : HistoryTableData

/**
 * Representa uma linha da tabela de histórico de Fundos de Investimento.
 */
public data class InvestmentFundHistoryTableData(
    override val currentEntry: HoldingHistoryEntry,
    override val brokerageName: String,
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
    override val issuerName: String,
    override val observations: String,
    override val previousValue: Double,
    override val currentValue: Double,
    override val appreciation: Double,
    override val editable: Boolean,
    override val totalContributions: Double,
    override val totalWithdrawals: Double,
) : HistoryTableData

