package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.naming.asLabel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * Dados estáticos só para previews Compose (sem [WalletFiltersCatalog], [Liquidity] nem `:features:naming`).
 * Evita `ExceptionInInitializerError` no classpath de preview do Android Studio.
 */
internal object WalletFiltersPreviewCatalog {

    private fun opt(id: String, short: String, full: String = short) = FilterOption(id, short, full)

    private val rfSubtypeSection =
        SubtypeSectionModel(
            classKind = AssetClassKind.FixedIncome,
            options = FixedIncomeSubType.entries.map { opt(it.asLabel(), it.asLabel()) }
        )

    private val rvSubtypeSection =
        SubtypeSectionModel(
            classKind = AssetClassKind.VariableIncome,
            options = VariableIncomeAssetType.entries.map { opt(it.asLabel(), it.asLabel()) }
        )

    private val fundsSubtypeSection =
        SubtypeSectionModel(
            classKind = AssetClassKind.Funds,
            options = InvestmentFundAssetType.entries.map { opt(it.asLabel(), it.asLabel()) }
        )

    private val liquidityAll =
        listOf(
            opt("liquidity:DAILY", "Diária"),
            opt("liquidity:AT_MATURITY", "Vencimento"),
        )

    private val b3Both =
        listOf(
            opt("b3:yes", "Sim", "Informado na B3"),
            opt("b3:no", "Não", "Não informado na B3"),
        )

    private val settledBoth =
        listOf(
            opt("settled:yes", "Sim", "Liquidado"),
            opt("settled:no", "Não", "Não liquidado"),
        )

    private val maturityFull =
        listOf(
            YearMonth(2026, Month.OCTOBER),
            YearMonth(2026, Month.DECEMBER),
            YearMonth(2027, Month.FEBRUARY),
            YearMonth(2027, Month.AUGUST),
            YearMonth(2027, Month.NOVEMBER),
            YearMonth(2028, Month.MARCH),
            YearMonth(2029, Month.JUNE),
            YearMonth(2030, Month.JANUARY),
        )

    val fullPanelOptions: WalletFiltersPanelOptions =
        WalletFiltersPanelOptions(
            classOptions = InvestmentCategory.entries.map { opt(it.formated(), it.formated()) } ,
            subtypeSections = listOf(rfSubtypeSection, rvSubtypeSection, fundsSubtypeSection),
            liquidityOptions = liquidityAll,
            b3Options = b3Both,
            settledOptions = settledBoth,
            maturityMonths = maturityFull,
        )
}
