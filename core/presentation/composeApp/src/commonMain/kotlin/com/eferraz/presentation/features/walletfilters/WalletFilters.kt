package com.eferraz.presentation.features.walletfilters

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.naming.asLabel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.datetime.YearMonth

internal enum class SubtypeKind {
    Cdb, Lci, Lca, Lig, Selic, Precatorio, Cri, Cra, TreasuryDirect,
    RealEstateFund, NationalStock, InternationalStock, Etf,
    StockFund, MultimarketFund, Pension,
}

internal data class FilterOption(val id: String, val shortLabel: String, val fullLabel: String)

/** Filtros comuns — classe, B3 informado e liquidados. */
@Immutable
internal data class WalletFiltersComunsSectionOptions(
    val classOptions: List<FilterOption> = emptyList(),
    val b3Options: List<FilterOption> = emptyList(),
    val settledOptions: List<FilterOption> = emptyList(),
)

/** Renda fixa — subtipos, liquidez e vencimento. */
@Immutable
internal data class WalletFiltersFixedIncomeSectionOptions(
    val subtypeOptions: List<FilterOption> = emptyList(),
    val liquidityOptions: List<FilterOption> = emptyList(),
    val maturityMonths: List<YearMonth> = emptyList(),
)

/** Renda variável — subtipos. */
@Immutable
internal data class WalletFiltersVariableIncomeSectionOptions(
    val subtypeOptions: List<FilterOption> = emptyList(),
)

/** Fundos — subtipos. */
@Immutable
internal data class WalletFiltersFundsSectionOptions(
    val subtypeOptions: List<FilterOption> = emptyList(),
)

@Immutable
internal data class WalletFiltersPanelOptions(
    val commons: WalletFiltersComunsSectionOptions = WalletFiltersComunsSectionOptions(),
    val fixedIncome: WalletFiltersFixedIncomeSectionOptions = WalletFiltersFixedIncomeSectionOptions(),
    val variableIncome: WalletFiltersVariableIncomeSectionOptions = WalletFiltersVariableIncomeSectionOptions(),
    val funds: WalletFiltersFundsSectionOptions = WalletFiltersFundsSectionOptions(),
)

@Immutable
internal data class WalletFiltersUiState(
    val selectedClassIds: Set<String> = emptySet(),
    val selectedSubtypeIds: Set<String> = emptySet(),
    val selectedLiquidityIds: Set<String> = emptySet(),
    val selectedB3Ids: Set<String> = emptySet(),
    val selectedSettledIds: Set<String> = emptySet(),
    val maturitySelection: YearMonth? = null,
) {
    companion object {
        fun initial(): WalletFiltersUiState = WalletFiltersUiState()
    }

    fun toggleClass(id: String): WalletFiltersUiState {
        val removing = id in selectedClassIds
        return copy(
            selectedClassIds = selectedClassIds.toggled(id),
            selectedSubtypeIds =
                if (removing) {
                    selectedSubtypeIds.filterNot {
                        WalletFiltersCatalog.categoryForSubtypeId(it) ==
                            WalletFiltersCatalog.categoryForClassId(id)
                    }.toSet()
                } else {
                    selectedSubtypeIds
                },
        )
    }

    fun toggleSubtype(id: String) = copy(selectedSubtypeIds = selectedSubtypeIds.toggled(id))
    fun toggleLiquidity(id: String) = copy(selectedLiquidityIds = selectedLiquidityIds.toggled(id))
    fun toggleB3(id: String) = copy(selectedB3Ids = selectedB3Ids.toggled(id))
    fun toggleSettled(id: String) = copy(selectedSettledIds = selectedSettledIds.toggled(id))
    fun selectMaturity(month: YearMonth?) = copy(maturitySelection = month)
    fun reset() = initial()
}

internal data class WalletFilterHoldingFacet(
    val category: InvestmentCategory,
    val subtype: SubtypeKind,
    val liquidity: Liquidity,
    val b3Informed: Boolean,
    val settled: Boolean,
    val maturity: YearMonth? = null,
)

internal object WalletFiltersCatalog {
    fun classId(category: InvestmentCategory) = category.name
    fun subtypeId(kind: SubtypeKind) = "subtype:${kind.name}"
    fun liquidityId(liquidity: Liquidity) = "liquidity:${liquidity.name}"
    fun b3Id(informed: Boolean) = "b3:${if (informed) "yes" else "no"}"
    fun settledId(settled: Boolean) = "settled:${if (settled) "yes" else "no"}"

    private val fixedIncomeSubtypes =
        setOf(
            SubtypeKind.Cdb, SubtypeKind.Lci, SubtypeKind.Lca, SubtypeKind.Lig,
            SubtypeKind.Selic, SubtypeKind.Precatorio, SubtypeKind.Cri, SubtypeKind.Cra, SubtypeKind.TreasuryDirect,
        )
    private val variableIncomeSubtypes =
        setOf(SubtypeKind.RealEstateFund, SubtypeKind.NationalStock, SubtypeKind.InternationalStock, SubtypeKind.Etf)

    fun categoryForClassId(classId: String): InvestmentCategory? =
        InvestmentCategory.entries.firstOrNull { classId(it) == classId }
            ?: classIdByDisplayLabel[classId]

    fun isClassSelected(category: InvestmentCategory, selectedClassIds: Set<String>): Boolean =
        selectedClassIds.any { categoryForClassId(it) == category }

    private val classIdByDisplayLabel: Map<String, InvestmentCategory> =
        mapOf(
            "Renda Fixa" to InvestmentCategory.FIXED_INCOME,
            "Renda Variável" to InvestmentCategory.VARIABLE_INCOME,
            "Fundos" to InvestmentCategory.INVESTMENT_FUND,
            "FixedIncome" to InvestmentCategory.FIXED_INCOME,
            "VariableIncome" to InvestmentCategory.VARIABLE_INCOME,
            "Funds" to InvestmentCategory.INVESTMENT_FUND,
        )

    fun categoryForSubtypeId(subtypeId: String): InvestmentCategory? =
        SubtypeKind.entries.firstOrNull { subtypeId(it) == subtypeId }?.let(::categoryForSubtype)

    fun categoryForSubtype(subtype: SubtypeKind): InvestmentCategory =
        when (subtype) {
            in fixedIncomeSubtypes -> InvestmentCategory.FIXED_INCOME
            in variableIncomeSubtypes -> InvestmentCategory.VARIABLE_INCOME
            else -> InvestmentCategory.INVESTMENT_FUND
        }

    val subtypesByCategory: Map<InvestmentCategory, List<SubtypeKind>> =
        SubtypeKind.entries.groupBy(::categoryForSubtype)

    fun classOption(category: InvestmentCategory) = category.toFilterOption(::classId)
    fun subtypeOption(kind: SubtypeKind) = kind.toFilterOption(::subtypeId)
    fun liquidityOption(liquidity: Liquidity) =
        FilterOption(liquidityId(liquidity), liquidity.asLabel(), liquidity.asLabel())

    fun b3Option(informed: Boolean) = simNaoOption(::b3Id, informed, "Informado na B3", "Não informado na B3")
    fun settledOption(settled: Boolean) = simNaoOption(::settledId, settled, "Liquidado", "Não liquidado")

    private fun simNaoOption(
        id: (Boolean) -> String,
        value: Boolean,
        yesFull: String,
        noFull: String,
    ) = FilterOption(
        id = id(value),
        shortLabel = if (value) "Sim" else "Não",
        fullLabel = if (value) yesFull else noFull,
    )
}

internal fun deriveFilterOptions(holdings: Iterable<WalletFilterHoldingFacet>): WalletFiltersPanelOptions {
    val categories = mutableSetOf<InvestmentCategory>()
    val b3 = mutableSetOf<Boolean>()
    val settled = mutableSetOf<Boolean>()
    val maturities = mutableSetOf<YearMonth>()

    holdings.forEach { h ->
        categories += h.category
        b3 += h.b3Informed
        settled += h.settled
        h.maturity?.let { maturities += it }
    }

    if (categories.isEmpty() && b3.isEmpty() && settled.isEmpty() && maturities.isEmpty()) {
        return WalletFiltersPanelOptions()
    }

    val liquidityOptions = Liquidity.entries.map(WalletFiltersCatalog::liquidityOption)
    val maturityMonths = maturities.sortedWith(compareBy({ it.year }, { it.month }))

    return WalletFiltersPanelOptions(
        commons =
            WalletFiltersComunsSectionOptions(
                classOptions = InvestmentCategory.entries.filter { it in categories }.map(WalletFiltersCatalog::classOption),
                b3Options = b3.map(WalletFiltersCatalog::b3Option),
                settledOptions = settled.map(WalletFiltersCatalog::settledOption),
            ),
        fixedIncome = buildFixedIncomeSection(liquidityOptions, maturityMonths),
        variableIncome = buildVariableIncomeSection(),
        funds = buildFundsSection(),
    )
}

internal fun buildFixedIncomeSection(
    liquidityOptions: List<FilterOption> = Liquidity.entries.map(WalletFiltersCatalog::liquidityOption),
    maturityMonths: List<YearMonth> = emptyList(),
): WalletFiltersFixedIncomeSectionOptions =
    WalletFiltersFixedIncomeSectionOptions(
        subtypeOptions = subtypeOptionsFor(InvestmentCategory.FIXED_INCOME),
        liquidityOptions = liquidityOptions,
        maturityMonths = maturityMonths,
    )

internal fun buildVariableIncomeSection(): WalletFiltersVariableIncomeSectionOptions =
    WalletFiltersVariableIncomeSectionOptions(
        subtypeOptions = subtypeOptionsFor(InvestmentCategory.VARIABLE_INCOME),
    )

internal fun buildFundsSection(): WalletFiltersFundsSectionOptions =
    WalletFiltersFundsSectionOptions(
        subtypeOptions = subtypeOptionsFor(InvestmentCategory.INVESTMENT_FUND),
    )

private fun subtypeOptionsFor(category: InvestmentCategory): List<FilterOption> =
    WalletFiltersCatalog.subtypesByCategory[category]
        .orEmpty()
        .map(WalletFiltersCatalog::subtypeOption)

internal fun WalletFiltersUiState.isClassSelected(category: InvestmentCategory): Boolean =
    WalletFiltersCatalog.isClassSelected(category, selectedClassIds)

private fun Set<String>.toggled(id: String) = if (id in this) this - id else this + id

private fun InvestmentCategory.toFilterOption(id: (InvestmentCategory) -> String) =
    FilterOption(id(this), shortLabel, formated())

private fun SubtypeKind.toFilterOption(id: (SubtypeKind) -> String) =
    FilterOption(id(this), shortLabel, fullLabel)

private val InvestmentCategory.shortLabel
    get() =
        when (this) {
            InvestmentCategory.FIXED_INCOME -> "RF"
            InvestmentCategory.VARIABLE_INCOME -> "RV"
            InvestmentCategory.INVESTMENT_FUND -> "Fundos"
        }

private val SubtypeKind.shortLabel
    get() = subtypeLabels[this]?.first ?: name

private val SubtypeKind.fullLabel
    get() = subtypeLabels[this]?.second ?: shortLabel

private fun lbl(short: String, full: String = short) = short to full

private val subtypeLabels: Map<SubtypeKind, Pair<String, String>> =
    mapOf(
        SubtypeKind.Cdb to lbl("CDB"),
        SubtypeKind.Lci to lbl("LCI"),
        SubtypeKind.Lca to lbl("LCA"),
        SubtypeKind.Lig to lbl("LIG"),
        SubtypeKind.Selic to lbl("Selic"),
        SubtypeKind.Precatorio to lbl("Prec.", "Precatório"),
        SubtypeKind.Cri to lbl("CRI"),
        SubtypeKind.Cra to lbl("CRA"),
        SubtypeKind.TreasuryDirect to lbl("TD", "Tesouro Direto"),
        SubtypeKind.RealEstateFund to lbl("FII", "Fundo imobiliário"),
        SubtypeKind.NationalStock to lbl("Ação BR", "Ação nacional"),
        SubtypeKind.InternationalStock to lbl("Ação INT", "Ação internacional"),
        SubtypeKind.Etf to lbl("ETF"),
        SubtypeKind.StockFund to lbl("Ações", "Fundo de ações"),
        SubtypeKind.MultimarketFund to lbl("Multi", "Fundo multimercado"),
        SubtypeKind.Pension to lbl("Prev.", "Previdência"),
    )
