package com.eferraz.presentation.features.history

import com.eferraz.entities.assets.YesOrNo
import com.eferraz.presentation.features.walletfilters.WalletFilterSubtype
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.usecases.screens.WalletHistoryFilterCriteria
import com.eferraz.usecases.screens.WalletHistorySubtype

internal fun WalletFiltersUiState.toWalletHistoryFilterCriteria(): WalletHistoryFilterCriteria =
    WalletHistoryFilterCriteria(
        categories = selectedCategories,
        subtypes = selectedSubtypes.map { it.toWalletHistorySubtype() }.toSet(),
        liquidities = selectedLiquidities,
        b3Informed = selectedB3.toBooleanSet(),
        settled = selectedSettled.toBooleanSet(),
        maturityUpTo = maturitySelection,
    )

private fun WalletFilterSubtype.toWalletHistorySubtype(): WalletHistorySubtype =
    when (this) {
        is WalletFilterSubtype.FixedIncome -> WalletHistorySubtype.FixedIncome(value)
        is WalletFilterSubtype.VariableIncome -> WalletHistorySubtype.VariableIncome(value)
        is WalletFilterSubtype.InvestmentFund -> WalletHistorySubtype.InvestmentFund(value)
    }

/** Sim → true, Não → false; Sim+Não → ambos (grupo saturado / inactivo no domínio). */
private fun Set<YesOrNo>.toBooleanSet(): Set<Boolean> =
    map { it == YesOrNo.YES }.toSet()
