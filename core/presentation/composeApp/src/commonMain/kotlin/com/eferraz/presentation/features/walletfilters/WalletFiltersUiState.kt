package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YesOrNo
import kotlinx.datetime.YearMonth

internal fun WalletFiltersUiState.toggleClass(category: InvestmentCategory): WalletFiltersUiState {
    val removing = category in selectedCategories
    return copy(
        selectedCategories = selectedCategories.toggle(category),
        selectedSubtypes =
            if (removing) {
                selectedSubtypes.filterNot { it.category() == category }.toSet()
            } else {
                selectedSubtypes
            },
    )
}

internal fun WalletFiltersUiState.toggleSubtype(subtype: WalletFilterSubtype): WalletFiltersUiState =
    copy(selectedSubtypes = selectedSubtypes.toggle(subtype))

internal fun WalletFiltersUiState.toggleLiquidity(liquidity: Liquidity): WalletFiltersUiState =
    copy(selectedLiquidities = selectedLiquidities.toggle(liquidity))

internal fun WalletFiltersUiState.toggleB3(value: YesOrNo): WalletFiltersUiState =
    copy(selectedB3 = selectedB3.toggle(value))

internal fun WalletFiltersUiState.toggleSettled(value: YesOrNo): WalletFiltersUiState =
    copy(selectedSettled = selectedSettled.toggle(value))

internal fun WalletFiltersUiState.selectMaturity(yearMonth: YearMonth): WalletFiltersUiState =
    copy(maturitySelection = yearMonth)

internal fun WalletFiltersUiState.selectMaturityAny(): WalletFiltersUiState =
    copy(maturitySelection = null)

internal fun WalletFiltersUiState.reset(): WalletFiltersUiState = WalletFiltersUiState.defaultForHistory()

internal fun WalletFiltersUiState.isClassSelected(category: InvestmentCategory): Boolean =
    category in selectedCategories

private fun <T> Set<T>.toggle(id: T): Set<T> = if (id in this) this - id else this + id
