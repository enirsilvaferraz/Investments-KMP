package com.eferraz.presentation.features.history

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.presentation.features.summary.SummaryProperties
import com.eferraz.presentation.features.walletfilters.WalletFiltersPanelOptions
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.usecases.entities.HoldingHistoryView
import kotlinx.datetime.YearMonth

internal data class HistoryState(
    val tableData: List<HoldingHistoryView> = emptyList(),
    val summaryProperties: SummaryProperties = SummaryProperties(),
    val selectedHolding: AssetHolding? = null,
    val period: Choice<YearMonth> = Choice(null, emptyList()),
    val brokerage: Choice<Brokerage> = Choice(null, emptyList()),
    val walletFilters: WalletFiltersUiState = WalletFiltersUiState.defaultForHistory(),
    val walletFilterOptions: WalletFiltersPanelOptions = WalletFiltersPanelOptions(),
    val transactions: List<AssetTransaction> = emptyList(),
    val isImporting: Boolean = false,
) {

    data class Choice<T>(
        val selected: T?,
        val options: List<T>,
    )
}
