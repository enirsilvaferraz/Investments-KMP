package com.eferraz.presentation.features.walletfilters

import kotlinx.datetime.YearMonth

internal fun WalletFiltersUiState.toggleClass(id: String): WalletFiltersUiState {
    val removing = id in selectedClassIds
    val nextClassIds =
        if (removing) selectedClassIds - id else selectedClassIds + id
    val nextSubtypeIds =
        if (removing) {
            selectedSubtypeIds.filterNot { subtypeId ->
                WalletFiltersCatalog.categoryForSubtypeId(subtypeId) ==
                    WalletFiltersCatalog.categoryForClassId(id)
            }.toSet()
        } else {
            selectedSubtypeIds
        }
    return copy(
        selectedClassIds = nextClassIds,
        selectedSubtypeIds = nextSubtypeIds,
    )
}

internal fun WalletFiltersUiState.toggleSubtype(id: String): WalletFiltersUiState =
    copy(selectedSubtypeIds = selectedSubtypeIds.toggle(id))

internal fun WalletFiltersUiState.toggleLiquidity(id: String): WalletFiltersUiState =
    copy(selectedLiquidityIds = selectedLiquidityIds.toggle(id))

internal fun WalletFiltersUiState.toggleB3(id: String): WalletFiltersUiState =
    copy(selectedB3Ids = selectedB3Ids.toggle(id))

internal fun WalletFiltersUiState.toggleSettled(id: String): WalletFiltersUiState =
    copy(selectedSettledIds = selectedSettledIds.toggle(id))

internal fun WalletFiltersUiState.selectMaturity(yearMonth: YearMonth): WalletFiltersUiState =
    copy(maturitySelection = yearMonth)

internal fun WalletFiltersUiState.selectMaturityAny(): WalletFiltersUiState =
    copy(maturitySelection = null)

internal fun WalletFiltersUiState.reset(): WalletFiltersUiState = WalletFiltersUiState.initial()

private fun Set<String>.toggle(id: String): Set<String> =
    if (id in this) this - id else this + id
