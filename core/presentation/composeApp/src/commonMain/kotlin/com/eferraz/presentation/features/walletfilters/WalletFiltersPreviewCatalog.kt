package com.eferraz.presentation.features.walletfilters

import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * Dados estáticos só para previews Compose.
 */
internal object WalletFiltersPreviewCatalog {

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
        WalletFiltersCatalog.staticPanelOptions(maturityMonths = maturityFull)
}
