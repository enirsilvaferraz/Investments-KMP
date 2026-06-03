package com.eferraz.presentation.features.walletfilters

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YesOrNo
import com.eferraz.naming.asLabel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.datetime.YearMonth

@Immutable
internal data class WalletFiltersPanelOptions(
    val commons: Commons = Commons(),
    val fixedIncome: FixedIncome = FixedIncome(),
    val variableIncome: VariableIncome = VariableIncome(),
    val funds: Funds = Funds(),
) {

    /** Filtros comuns — classe, B3 informado e liquidados. */
    @Immutable
    internal data class Commons(
        val classOptions: List<FilterOption<AssetClass>> = emptyList(),
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
    val selectedSettled: Set<YesOrNo> = emptySet(),
    val maturitySelection: YearMonth? = null,
) {
    companion object {
        fun initial(): WalletFiltersUiState = WalletFiltersUiState()

        /** Default do histórico: só «Não liquidado» activo (FR-008 / SC-004). */
        fun defaultForHistory(): WalletFiltersUiState =
            WalletFiltersUiState(selectedSettled = setOf(YesOrNo.NO))
    }
}

internal data class WalletFilterHoldingFacet(
    val assetClass: AssetClass,
    val subtype: WalletFilterSubtype,
    val liquidity: Liquidity,
    val b3Informed: YesOrNo,
    val settled: YesOrNo,
    val maturity: YearMonth? = null,
)

internal object WalletFiltersCatalog {

    fun classOption(assetClass: AssetClass): FilterOption<AssetClass> =
        FilterOption(
            id = assetClass,
            shortLabel = assetClass.shortLabel,
            fullLabel = assetClass.formated(),
        )

    fun subtypeOption(subtype: WalletFilterSubtype): FilterOption<WalletFilterSubtype> =
        FilterOption(
            id = subtype,
            shortLabel = subtype.shortLabel,
            fullLabel = subtype.fullLabel,
        )

    fun liquidityOption(liquidity: Liquidity): FilterOption<Liquidity> =
        FilterOption(
            id = liquidity,
            shortLabel = liquidity.asLabel(),
            fullLabel = liquidity.asLabel(),
        )

    fun b3Option(value: YesOrNo): FilterOption<YesOrNo> =
        FilterOption(
            id = value,
            shortLabel = value.asLabel(),
            fullLabel = b3FullLabels.getValue(value),
        )

    fun settledOption(value: YesOrNo): FilterOption<YesOrNo> =
        FilterOption(
            id = value,
            shortLabel = value.asLabel(),
            fullLabel = settledFullLabels.getValue(value),
        )

    private val b3FullLabels: Map<YesOrNo, String> =
        mapOf(
            YesOrNo.YES to "Informado na B3",
            YesOrNo.NO to "Não informado na B3",
        )

    private val settledFullLabels: Map<YesOrNo, String> =
        mapOf(
            YesOrNo.YES to "Liquidado",
            YesOrNo.NO to "Não liquidado",
        )
}

private val AssetClass.shortLabel
    get() =
        when (this) {
            AssetClass.FIXED_INCOME -> "RF"
            AssetClass.VARIABLE_INCOME -> "RV"
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

/**
 * Deriva opções do painel a partir das facetas do mês (FR-018/FR-012).
 *
 * Selecções inválidas no [WalletFiltersUiState] não são podadas aqui; o match em
 * domain ignora IDs ausentes das opções derivadas.
 */
internal fun deriveWalletFiltersPanelOptions(
    facets: List<WalletFilterHoldingFacet>,
    maturityMonths: List<YearMonth>,
): WalletFiltersPanelOptions {
    if (facets.isEmpty()) return WalletFiltersPanelOptions()

    val categories = mutableSetOf<AssetClass>()
    val subtypesByAssetClass = mutableMapOf<AssetClass, MutableSet<WalletFilterSubtype>>()
    val liquidities = mutableSetOf<Liquidity>()
    val b3Values = mutableSetOf<YesOrNo>()
    val settledValues = mutableSetOf<YesOrNo>()

    for (facet in facets) {
        categories += facet.assetClass
        subtypesByAssetClass.getOrPut(facet.assetClass) { mutableSetOf() } += facet.subtype
        liquidities += facet.liquidity
        b3Values += facet.b3Informed
        settledValues += facet.settled
    }

    val subtypesPresent = subtypesByAssetClass.mapValues { it.value.toSet() }

    return WalletFiltersPanelOptions(
        commons =
            WalletFiltersPanelOptions.Commons(
                classOptions =
                    AssetClass.entries
                        .filter { it in categories }
                        .map(WalletFiltersCatalog::classOption),
                b3Options = deriveSimNaoToggleOptions(b3Values, WalletFiltersCatalog::b3Option),
                settledOptions = deriveSimNaoToggleOptions(settledValues, WalletFiltersCatalog::settledOption),
            ),
        fixedIncome =
            WalletFiltersPanelOptions.FixedIncome(
                subtypeOptions = subtypeOptionsForAssetClass(AssetClass.FIXED_INCOME, subtypesPresent),
                liquidityOptions =
                    Liquidity.entries
                        .filter { it in liquidities }
                        .map(WalletFiltersCatalog::liquidityOption),
                maturityMonths = maturityMonths,
            ),
        variableIncome =
            WalletFiltersPanelOptions.VariableIncome(
                subtypeOptions = subtypeOptionsForAssetClass(AssetClass.VARIABLE_INCOME, subtypesPresent),
            ),
        funds =
            WalletFiltersPanelOptions.Funds(
                subtypeOptions = subtypeOptionsForAssetClass(AssetClass.INVESTMENT_FUND, subtypesPresent),
            ),
    )
}

private fun subtypeOptionsForAssetClass(
    assetClass: AssetClass,
    present: Map<AssetClass, Set<WalletFilterSubtype>>,
): List<FilterOption<WalletFilterSubtype>> {
    val presentSubtypes = present[assetClass].orEmpty()
    if (presentSubtypes.isEmpty()) return emptyList()
    return subtypesByAssetClass[assetClass]
        .orEmpty()
        .filter { it in presentSubtypes }
        .map(WalletFiltersCatalog::subtypeOption)
}

/** FR-018d: secção oculta unless both Sim and Não exist in portfolio data. */
private fun deriveSimNaoToggleOptions(
    values: Set<YesOrNo>,
    optionFor: (YesOrNo) -> FilterOption<YesOrNo>,
): List<FilterOption<YesOrNo>> {
    if (YesOrNo.YES !in values || YesOrNo.NO !in values) return emptyList()
    return YesOrNo.entries.map(optionFor)
}
