package com.eferraz.asset_management.vm

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
internal sealed class VMEvents {
    data object ScreenEntered : VMEvents()

    data class CategoryChanged(val category: InvestmentCategory) : VMEvents()
    data class IssuerChanged(val issuer: Issuer) : VMEvents()
    data class ObservationsChanged(val value: String) : VMEvents()
    data class BrokerageChanged(val brokerage: Brokerage) : VMEvents()

    data class FixedTypeChanged(val type: FixedIncomeAssetType) : VMEvents()
    data class FixedSubTypeChanged(val subType: FixedIncomeSubType) : VMEvents()
    data class FixedExpirationChanged(val raw: String) : VMEvents()
    data class FixedYieldChanged(val value: String) : VMEvents()
    data class FixedCdiChanged(val value: String) : VMEvents()
    data class FixedLiquidityChanged(val liquidity: Liquidity) : VMEvents()

    data class VariableTypeChanged(val type: VariableIncomeAssetType) : VMEvents()
    data class VariableTickerChanged(val value: String) : VMEvents()
    data class VariableCnpjChanged(val value: String) : VMEvents()

    data class FundNameChanged(val value: String) : VMEvents()
    data class FundTypeChanged(val type: InvestmentFundAssetType) : VMEvents()
    data class FundLiquidityDaysChanged(val value: String) : VMEvents()
    data class FundExpirationChanged(val raw: String) : VMEvents()

    data object Save : VMEvents()
    data object RequestDismiss : VMEvents()
    data object NavigationConsumed : VMEvents()
}