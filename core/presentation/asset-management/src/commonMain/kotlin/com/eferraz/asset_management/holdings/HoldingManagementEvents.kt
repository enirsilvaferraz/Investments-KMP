package com.eferraz.asset_management.holdings

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage

internal sealed class HoldingManagementEvents {
    data class ScreenEntered(val holding: AssetHolding) : HoldingManagementEvents()
    data class BrokerageChanged(val brokerage: Brokerage) : HoldingManagementEvents()
    data object Save : HoldingManagementEvents()
}