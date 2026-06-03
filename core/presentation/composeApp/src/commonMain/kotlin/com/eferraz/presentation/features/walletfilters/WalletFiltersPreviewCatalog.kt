package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YesOrNo
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * Dados estáticos só para previews Compose.
 * Opções alinhadas a [WalletFiltersCatalog] e enums de `:domain:entity`.
 */
internal object WalletFiltersPreviewCatalog {

    private val rfSubtypeOptions =
        subtypesByCategory[InvestmentCategory.FIXED_INCOME]
            .orEmpty()
            .map(WalletFiltersCatalog::subtypeOption)

    private val rvSubtypeOptions =
        subtypesByCategory[InvestmentCategory.VARIABLE_INCOME]
            .orEmpty()
            .map(WalletFiltersCatalog::subtypeOption)

    private val fundsSubtypeOptions =
        subtypesByCategory[InvestmentCategory.INVESTMENT_FUND]
            .orEmpty()
            .map(WalletFiltersCatalog::subtypeOption)

    private val liquidityAll =
        Liquidity.entries
            .filter { it != Liquidity.D_PLUS_DAYS }
            .map(WalletFiltersCatalog::liquidityOption)

    private val b3Both = YesOrNo.entries.map(WalletFiltersCatalog::b3Option)

    private val settledBoth = YesOrNo.entries.map(WalletFiltersCatalog::settledOption)

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

    private val classOptions =
        InvestmentCategory.entries.map(WalletFiltersCatalog::classOption)

    val fullPanelOptions: WalletFiltersPanelOptions =
        WalletFiltersPanelOptions(
            commons = WalletFiltersPanelOptions.Commons(
                classOptions = classOptions,
                b3Options = b3Both,
                settledOptions = settledBoth,
            ),
            fixedIncome = WalletFiltersPanelOptions.FixedIncome(
                subtypeOptions = rfSubtypeOptions,
                liquidityOptions = liquidityAll,
                maturityMonths = maturityFull,
            ),
            variableIncome = WalletFiltersPanelOptions.VariableIncome(
                subtypeOptions = rvSubtypeOptions,
            ),
            funds = WalletFiltersPanelOptions.Funds(
                subtypeOptions = fundsSubtypeOptions,
            ),
        )
}
