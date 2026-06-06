package com.eferraz.asset_management.assets

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.AssetType
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.transactions.TransactionType

internal sealed class AssetManagementEvents {

    data class ScreenEntered(val holdingId: Long?) : AssetManagementEvents()
    data class BrokerageChanged(val brokerage: Brokerage) : AssetManagementEvents()

    data class AssetClassChanged(val assetClass: AssetClass) : AssetManagementEvents()
    data class IssuerChanged(val issuer: Issuer) : AssetManagementEvents()
    data class ObservationsChanged(val value: String) : AssetManagementEvents()
    data class B3IdentifierChanged(val value: String) : AssetManagementEvents()

    data class YieldIndexerChanged(val indexer: YieldIndexer) : AssetManagementEvents()
    data class TypeChanged(val type: AssetType) : AssetManagementEvents()
    data class FixedExpirationChanged(val raw: String) : AssetManagementEvents()
    data class FixedYieldChanged(val value: String) : AssetManagementEvents()
    data class FixedCdiChanged(val value: String) : AssetManagementEvents()
    data class FixedLiquidityChanged(val liquidity: Liquidity) : AssetManagementEvents()
    data class IncomeTaxExemptChanged(val exempt: Boolean) : AssetManagementEvents()

//    data class VariableTypeChanged(val type: VariableIncomeAssetType) : AssetManagementEvents()
    data class VariableTickerChanged(val value: String) : AssetManagementEvents()
    data class VariableCnpjChanged(val value: String) : AssetManagementEvents()

    data class FundNameChanged(val value: String) : AssetManagementEvents()
//    data class FundLiquidityChanged(val liquidity: Liquidity) : AssetManagementEvents()
//    data class FundTypeChanged(val type: InvestmentFundAssetType) : AssetManagementEvents()
//    data class FundLiquidityDaysChanged(val value: String) : AssetManagementEvents()
//    data class FundExpirationChanged(val raw: String) : AssetManagementEvents()

    data object Save : AssetManagementEvents()

    data class TransactionAdded(val assetClass: AssetClass) : AssetManagementEvents()
    data class TransactionRemoved(val index: Int) : AssetManagementEvents()
    data class TransactionDateChanged(val index: Int, val digits: String) : AssetManagementEvents()
    data class TransactionTypeChanged(val index: Int, val type: TransactionType) : AssetManagementEvents()
    data class TransactionQuantityChanged(val index: Int, val value: String) : AssetManagementEvents()
    data class TransactionUnitPriceChanged(val index: Int, val value: String) : AssetManagementEvents()
}
