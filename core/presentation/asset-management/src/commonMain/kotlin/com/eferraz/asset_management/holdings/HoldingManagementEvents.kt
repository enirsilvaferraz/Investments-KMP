package com.eferraz.asset_management.holdings

import com.eferraz.entities.holdings.Brokerage

internal sealed class HoldingManagementEvents {
    data class ScreenEntered(val holdingId: Long?) : HoldingManagementEvents()
    data class BrokerageChanged(val brokerage: Brokerage) : HoldingManagementEvents()
    data object Save : HoldingManagementEvents()
}