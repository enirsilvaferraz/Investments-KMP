package com.eferraz.presentation.features.walletfilters

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YesOrNo
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.naming.asLabel
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.SequenceMonths
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

@Immutable
internal data class WalletFiltersPanelOptions(
    val commons: Commons = Commons(),
    val fixedIncome: FixedIncome = FixedIncome(),
    val variableIncome: VariableIncome = VariableIncome(),
    val funds: Funds = Funds(),
) {

    /** Filtros comuns — classe, corretora, B3 informado e liquidados. */
    @Immutable
    internal data class Commons(
        val classOptions: List<FilterOption<AssetClass>> = emptyList(),
        val brokerageOptions: List<FilterOption<Brokerage>> = emptyList(),
        val b3Options: List<FilterOption<YesOrNo>> = emptyList(),
        val settledOptions: List<FilterOption<YesOrNo>> = emptyList(),
    )

    /** Renda fixa — subtipos, liquidez e vencimento. */
    @Immutable
    internal data class FixedIncome(
        val subtypeOptions: List<FilterOption<WalletFilterSubtype>> = emptyList(),
        val liquidityOptions: List<FilterOption<Liquidity>> = emptyList(),
        val maturityMonths: List<YearMonth> = emptyList(),
    )

    /** Renda variável — subtipos. */
    @Immutable
    internal data class VariableIncome(
        val subtypeOptions: List<FilterOption<WalletFilterSubtype>> = emptyList(),
    )

    /** Fundos — subtipos. */
    @Immutable
    internal data class Funds(
        val subtypeOptions: List<FilterOption<WalletFilterSubtype>> = emptyList(),
    )
}

@Immutable
internal data class WalletFiltersUiState(
    val selectedCategories: Set<AssetClass> = emptySet(),
    val selectedSubtypes: Set<WalletFilterSubtype> = emptySet(),
    val selectedLiquidities: Set<Liquidity> = emptySet(),
    val selectedB3: Set<YesOrNo> = emptySet(),
    val selectedSettled: Set<YesOrNo> = setOf(YesOrNo.NO),
    val maturitySelection: YearMonth? = null,
    val selectedBrokerage: Brokerage? = null,
)

internal object WalletFiltersCatalog {

    fun classOption(assetClass: AssetClass): FilterOption<AssetClass> =
        FilterOption(id = assetClass, shortLabel = assetClass.shortLabel, fullLabel = assetClass.formated())

    fun brokerageOption(brokerage: Brokerage): FilterOption<Brokerage> =
        FilterOption(id = brokerage, shortLabel = brokerage.name, fullLabel = brokerage.name)

    fun subtypeOption(subtype: WalletFilterSubtype): FilterOption<WalletFilterSubtype> =
        FilterOption(id = subtype, shortLabel = subtype.shortLabel, fullLabel = subtype.fullLabel)

    fun liquidityOption(liquidity: Liquidity): FilterOption<Liquidity> =
        FilterOption(id = liquidity, shortLabel = liquidity.asLabel(), fullLabel = liquidity.asLabel())

    fun b3Option(value: YesOrNo): FilterOption<YesOrNo> =
        FilterOption(id = value, shortLabel = value.asLabel(), fullLabel = b3FullLabels.getValue(value))

    fun settledOption(value: YesOrNo): FilterOption<YesOrNo> =
        FilterOption(id = value, shortLabel = value.asLabel(), fullLabel = settledFullLabels.getValue(value))

    private val b3FullLabels: Map<YesOrNo, String> =
        mapOf(YesOrNo.YES to "Informado na B3", YesOrNo.NO to "Não informado na B3")

    private val settledFullLabels: Map<YesOrNo, String> =
        mapOf(YesOrNo.YES to "Liquidado", YesOrNo.NO to "Não liquidado")

    /** Opções fixas do painel (enums + vencimentos); corretoras vêm à parte (ex. repositório). */
    fun staticPanelOptions(
        today: YearMonth,
        brokerages: List<Brokerage>,
    ): WalletFiltersPanelOptions =

        WalletFiltersPanelOptions(
            commons = WalletFiltersPanelOptions.Commons(
                classOptions = AssetClass.entries.map(::classOption),
                brokerageOptions = brokerages.map(::brokerageOption),
                b3Options = YesOrNo.entries.map(::b3Option),
                settledOptions = YesOrNo.entries.map(::settledOption),
            ),
            fixedIncome = WalletFiltersPanelOptions.FixedIncome(
                subtypeOptions = subtypesByAssetClass[AssetClass.FIXED_INCOME].orEmpty().map(::subtypeOption),
                liquidityOptions = Liquidity.entries.filter { it != Liquidity.D_PLUS_DAYS }.map(::liquidityOption),
                maturityMonths =  SequenceMonths.build(today, today.plus(30, DateTimeUnit.MONTH)).entries,
            ),
            variableIncome = WalletFiltersPanelOptions.VariableIncome(
                subtypeOptions = subtypesByAssetClass[AssetClass.VARIABLE_INCOME].orEmpty().map(::subtypeOption),
            ),
            funds = WalletFiltersPanelOptions.Funds(
                subtypeOptions = subtypesByAssetClass[AssetClass.INVESTMENT_FUND].orEmpty().map(::subtypeOption),
            ),
        )
}

private val AssetClass.shortLabel
    get() =
        when (this) {
            AssetClass.FIXED_INCOME -> "Renda Fixa"
            AssetClass.VARIABLE_INCOME -> "R. Variável"
            AssetClass.INVESTMENT_FUND -> "Fundos"
        }

private val WalletFilterSubtype.shortLabel: String
    get() =
        when (this) {
            is WalletFilterSubtype.FixedIncome ->
                when (value) {
                    FixedIncomeAssetType.CDB -> "CDB"
                    FixedIncomeAssetType.LCI -> "LCI"
                    FixedIncomeAssetType.LCA -> "LCA"
                    FixedIncomeAssetType.LIG -> "LIG"
                    FixedIncomeAssetType.DEBENTURE -> "Deb."
                    FixedIncomeAssetType.SELIC -> "Selic"
                    FixedIncomeAssetType.PRECATORIO -> "Prec."
                }

            is WalletFilterSubtype.VariableIncome ->
                when (value) {
                    VariableIncomeAssetType.NATIONAL_STOCK -> "Ação BR"
                    VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ação INT"
                    VariableIncomeAssetType.REAL_ESTATE_FUND -> "FII"
                    VariableIncomeAssetType.ETF -> "ETF"
                }

            is WalletFilterSubtype.InvestmentFund ->
                when (value) {
                    InvestmentFundAssetType.STOCK_FUND -> "Ações"
                    InvestmentFundAssetType.MULTIMARKET_FUND -> "Multi"
                    InvestmentFundAssetType.PENSION -> "Prev."
                }
        }

private val WalletFilterSubtype.fullLabel: String
    get() =
        when (this) {
            is WalletFilterSubtype.FixedIncome -> value.asLabel()
            is WalletFilterSubtype.VariableIncome -> value.asLabel()
            is WalletFilterSubtype.InvestmentFund -> value.asLabel()
        }
