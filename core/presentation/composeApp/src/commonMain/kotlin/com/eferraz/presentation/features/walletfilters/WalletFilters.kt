package com.eferraz.presentation.features.walletfilters

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.Liquidity
import com.eferraz.naming.asLabel
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal enum class AssetClassKind { FixedIncome, VariableIncome, Funds }

internal enum class SubtypeKind {
    Cdb, Lci, Lca, Lig, Selic, Precatorio, Cri, Cra, TreasuryDirect,
    RealEstateFund, NationalStock, InternationalStock, Etf,
    StockFund, MultimarketFund, Pension,
}

internal data class FilterOption(val id: String, val shortLabel: String, val fullLabel: String)

@Immutable
internal data class SubtypeSectionModel(val classKind: AssetClassKind, val options: List<FilterOption>) {
    val title: String get() = classKind.fullLabel
}

@Immutable
internal data class WalletFiltersPanelOptions(
    val classOptions: List<FilterOption> = emptyList(),
    val subtypeSections: List<SubtypeSectionModel> = emptyList(),
    val liquidityOptions: List<FilterOption> = emptyList(),
    val b3Options: List<FilterOption> = emptyList(),
    val settledOptions: List<FilterOption> = emptyList(),
    val maturityMonths: List<YearMonth> = emptyList(),
)

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
                        WalletFiltersCatalog.assetClassForSubtypeId(it) ==
                            WalletFiltersCatalog.assetClassForClassId(id)
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
    val assetClass: AssetClassKind,
    val subtype: SubtypeKind,
    val liquidity: Liquidity,
    val b3Informed: Boolean,
    val settled: Boolean,
    val maturity: YearMonth? = null,
)

internal object WalletFiltersCatalog {
    fun classId(kind: AssetClassKind) = kind.name
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

    fun assetClassForClassId(classId: String) =
        AssetClassKind.entries.firstOrNull { classId(it) == classId }

    fun assetClassForSubtypeId(subtypeId: String) =
        SubtypeKind.entries.firstOrNull { subtypeId(it) == subtypeId }?.let(::assetClassForSubtype)

    fun assetClassForSubtype(subtype: SubtypeKind): AssetClassKind =
        when (subtype) {
            in fixedIncomeSubtypes -> AssetClassKind.FixedIncome
            in variableIncomeSubtypes -> AssetClassKind.VariableIncome
            else -> AssetClassKind.Funds
        }

    val subtypesByClass: Map<AssetClassKind, List<SubtypeKind>> =
        SubtypeKind.entries.groupBy(::assetClassForSubtype)

    fun classOption(kind: AssetClassKind) = kind.toFilterOption(::classId)
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
    val classes = mutableSetOf<AssetClassKind>()
    val subtypesByClass = mutableMapOf<AssetClassKind, MutableSet<SubtypeKind>>()
    val liquidities = mutableSetOf<Liquidity>()
    val b3 = mutableSetOf<Boolean>()
    val settled = mutableSetOf<Boolean>()
    val maturities = mutableSetOf<YearMonth>()

    holdings.forEach { h ->
        classes += h.assetClass
        subtypesByClass.getOrPut(h.assetClass) { mutableSetOf() } += h.subtype
        liquidities += h.liquidity
        b3 += h.b3Informed
        settled += h.settled
        h.maturity?.let { maturities += it }
    }

    if (classes.isEmpty() && liquidities.isEmpty() && b3.isEmpty() && settled.isEmpty() && maturities.isEmpty()) {
        return WalletFiltersPanelOptions()
    }

    return WalletFiltersPanelOptions(
        classOptions = AssetClassKind.entries.filter { it in classes }.map(WalletFiltersCatalog::classOption),
        subtypeSections = buildSubtypeSections(classes, subtypesByClass.mapValues { it.value.toSet() }),
        liquidityOptions = Liquidity.entries.filter { it in liquidities }.map(WalletFiltersCatalog::liquidityOption),
        b3Options = b3.map(WalletFiltersCatalog::b3Option),
        settledOptions = settled.map(WalletFiltersCatalog::settledOption),
        maturityMonths = maturities.sortedWith(compareBy({ it.year }, { it.month })),
    )
}

internal fun buildSubtypeSections(
    classes: Set<AssetClassKind>,
    subtypesByClass: Map<AssetClassKind, Set<SubtypeKind>>,
): List<SubtypeSectionModel> =
    AssetClassKind.entries.filter { it in classes }.mapNotNull { classKind ->
        val options =
            WalletFiltersCatalog.subtypesByClass[classKind]
                .orEmpty()
                .filter { it in subtypesByClass[classKind].orEmpty() }
                .map(WalletFiltersCatalog::subtypeOption)
        options.takeIf { it.isNotEmpty() }?.let { SubtypeSectionModel(classKind, it) }
    }

internal fun activeSubtypeSections(sections: List<SubtypeSectionModel>, activeClassIds: Set<String>) =
    sections.filter { WalletFiltersCatalog.classId(it.classKind) in activeClassIds && it.options.isNotEmpty() }

/** Opções já filtradas para o que o painel deve renderizar (ex.: subtipos das classes activas). */
internal fun WalletFiltersPanelOptions.forDisplay(state: WalletFiltersUiState): WalletFiltersPanelOptions =
    copy(subtypeSections = activeSubtypeSections(subtypeSections, state.selectedClassIds))

private fun Set<String>.toggled(id: String) = if (id in this) this - id else this + id

private fun AssetClassKind.toFilterOption(id: (AssetClassKind) -> String) =
    FilterOption(id(this), shortLabel, fullLabel)

private fun SubtypeKind.toFilterOption(id: (SubtypeKind) -> String) =
    FilterOption(id(this), shortLabel, fullLabel)

private val AssetClassKind.shortLabel
    get() = when (this) { AssetClassKind.FixedIncome -> "RF"; AssetClassKind.VariableIncome -> "RV"; AssetClassKind.Funds -> "Fundos" }

private val AssetClassKind.fullLabel
    get() = when (this) { AssetClassKind.FixedIncome -> "Renda Fixa"; AssetClassKind.VariableIncome -> "Renda Variável"; AssetClassKind.Funds -> "Fundos" }

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
