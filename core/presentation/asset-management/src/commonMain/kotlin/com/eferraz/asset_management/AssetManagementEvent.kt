package com.eferraz.asset_management

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

/**
 * Ações do ecrã de registo (formulário e navegação) — ponto de entrada único [AssetManagementViewModel.dispatch].
 */
internal sealed class AssetManagementEvent {
    // Formulário
    data class CategoryChanged(val category: InvestmentCategory) : AssetManagementEvent()
    data class IssuerChanged(val issuer: Issuer) : AssetManagementEvent()
    data class ObservationsChanged(val value: String) : AssetManagementEvent()
    data class BrokerageChanged(val brokerage: Brokerage) : AssetManagementEvent()

    data class FixedTypeChanged(val type: FixedIncomeAssetType) : AssetManagementEvent()
    data class FixedSubTypeChanged(val subType: FixedIncomeSubType) : AssetManagementEvent()
    data class FixedExpirationChanged(val raw: String) : AssetManagementEvent()
    data class FixedYieldChanged(val value: String) : AssetManagementEvent()
    data class FixedCdiChanged(val value: String) : AssetManagementEvent()
    data class FixedLiquidityChanged(val liquidity: Liquidity) : AssetManagementEvent()

    data class VariableTypeChanged(val type: VariableIncomeAssetType) : AssetManagementEvent()
    data class VariableTickerChanged(val value: String) : AssetManagementEvent()
    data class VariableCnpjChanged(val value: String) : AssetManagementEvent()

    data class FundNameChanged(val value: String) : AssetManagementEvent()
    data class FundTypeChanged(val type: InvestmentFundAssetType) : AssetManagementEvent()
    data class FundLiquidityDaysChanged(val value: String) : AssetManagementEvent()
    data class FundExpirationChanged(val raw: String) : AssetManagementEvent()

    data object Save : AssetManagementEvent()
    data object RequestDismiss : AssetManagementEvent()
    data object NavigationConsumed : AssetManagementEvent()
}
