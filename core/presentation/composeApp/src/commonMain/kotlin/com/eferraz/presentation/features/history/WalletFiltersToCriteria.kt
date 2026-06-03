package com.eferraz.presentation.features.history

import com.eferraz.entities.assets.YesOrNo
import com.eferraz.presentation.features.walletfilters.WalletFilterSubtype
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.usecases.screens.WalletHistoryFilterCriteria
import com.eferraz.usecases.screens.WalletHistorySubtype

internal fun WalletFiltersUiState.toWalletHistoryFilterCriteria(): WalletHistoryFilterCriteria =
    WalletHistoryFilterCriteria(
        assetClasses = selectedCategories,
        subtypes = selectedSubtypes.map { it.toWalletHistorySubtype() }.toSet(),
        liquidities = selectedLiquidities,
        b3Informed = selectedB3.toBooleanSet(),
        settled = selectedSettled.toBooleanSet(),
        maturityUpTo = maturitySelection,
        brokerageIds = selectedBrokerage?.id?.let { setOf(it) } ?: emptySet(),
    )

/** Facetas do painel: só corretora activa; grupos do painel inactivos (FR-009). */
internal fun WalletFiltersUiState.facetCriteriaForPanelOptions(): WalletHistoryFilterCriteria =
    WalletHistoryFilterCriteria(
        brokerageIds = selectedBrokerage?.id?.let { setOf(it) } ?: emptySet(),
    )

/** Opções de corretora: critérios do painel activos; corretora inactiva (paridade com classe). */
internal fun WalletFiltersUiState.facetCriteriaForBrokerageOptions(): WalletHistoryFilterCriteria =
    toWalletHistoryFilterCriteria().copy(brokerageIds = emptySet())

private fun WalletFilterSubtype.toWalletHistorySubtype(): WalletHistorySubtype =
    when (this) {
        is WalletFilterSubtype.FixedIncome -> WalletHistorySubtype.FixedIncome(value)
        is WalletFilterSubtype.VariableIncome -> WalletHistorySubtype.VariableIncome(value)
        is WalletFilterSubtype.InvestmentFund -> WalletHistorySubtype.InvestmentFund(value)
    }

/** Sim → true, Não → false; Sim+Não → ambos (grupo saturado / inactivo no domínio). */
private fun Set<YesOrNo>.toBooleanSet(): Set<Boolean> =
    map { it == YesOrNo.YES }.toSet()
