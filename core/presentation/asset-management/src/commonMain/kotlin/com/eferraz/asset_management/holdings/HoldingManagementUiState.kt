package com.eferraz.asset_management.holdings

import androidx.compose.runtime.Immutable
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage

@Immutable
internal data class HoldingManagementUiState(
    val holding: AssetHolding? = null,
    val brokerage: Brokerage? = null,
    val brokerages: List<Brokerage> = emptyList(),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val brokerageError: String? = null,
) {

    internal fun withClearedFieldErrors() = copy(
        brokerageError = null,
    )

    internal fun hasAnyFieldError(): Boolean =
        brokerageError != null
}