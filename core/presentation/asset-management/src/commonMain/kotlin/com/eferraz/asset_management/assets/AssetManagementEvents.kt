package com.eferraz.asset_management.assets

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

internal sealed class AssetManagementEvents {

    data class ScreenEntered(val holdingId: Long?) : AssetManagementEvents()
    data class BrokerageChanged(val brokerage: Brokerage) : AssetManagementEvents()

    data class CategoryChanged(val category: InvestmentCategory) : AssetManagementEvents()
    data class IssuerChanged(val issuer: Issuer) : AssetManagementEvents()
    data class ObservationsChanged(val value: String) : AssetManagementEvents()

    data class FixedTypeChanged(val type: FixedIncomeAssetType) : AssetManagementEvents()
    data class FixedSubTypeChanged(val subType: FixedIncomeSubType) : AssetManagementEvents()
    data class FixedExpirationChanged(val raw: String) : AssetManagementEvents()
    data class FixedYieldChanged(val value: String) : AssetManagementEvents()
    data class FixedCdiChanged(val value: String) : AssetManagementEvents()
    data class FixedLiquidityChanged(val liquidity: Liquidity) : AssetManagementEvents()

    data class VariableTypeChanged(val type: VariableIncomeAssetType) : AssetManagementEvents()
    data class VariableTickerChanged(val value: String) : AssetManagementEvents()
    data class VariableCnpjChanged(val value: String) : AssetManagementEvents()

    data class FundNameChanged(val value: String) : AssetManagementEvents()
    data class FundTypeChanged(val type: InvestmentFundAssetType) : AssetManagementEvents()
    data class FundLiquidityDaysChanged(val value: String) : AssetManagementEvents()
    data class FundExpirationChanged(val raw: String) : AssetManagementEvents()

    data object Save : AssetManagementEvents()
}