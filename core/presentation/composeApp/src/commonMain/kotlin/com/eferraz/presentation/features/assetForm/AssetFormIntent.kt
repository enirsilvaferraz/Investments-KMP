package com.eferraz.presentation.features.assetForm

import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAssetType

internal sealed class AssetFormIntent {

    // Fixed Income
    data class UpdateType(val type: FixedIncomeAssetType?) : AssetFormIntent()
    data class UpdateSubType(val subType: FixedIncomeSubType?) : AssetFormIntent()
    data class UpdateExpirationDate(val date: String?) : AssetFormIntent()
    data class UpdateContractedYield(val yield: String) : AssetFormIntent()
    data class UpdateCdiRelativeYield(val yield: String) : AssetFormIntent()
    data class UpdateLiquidity(val liquidity: Liquidity?) : AssetFormIntent()

    // Investment Fund
    data class UpdateFundType(val type: InvestmentFundAssetType?) : AssetFormIntent()
    data class UpdateFundName(val name: String) : AssetFormIntent()
    data class UpdateLiquidityDays(val days: String) : AssetFormIntent()
    data class UpdateFundExpirationDate(val date: String?) : AssetFormIntent()

    // Variable Income
    data class UpdateVariableType(val type: VariableIncomeAssetType?) : AssetFormIntent()
    data class UpdateTicker(val ticker: String) : AssetFormIntent()

    // Common
    data class UpdateIssuerName(val name: String) : AssetFormIntent()
    data class UpdateObservations(val observations: String) : AssetFormIntent()
    data class UpdateBrokerageName(val name: String) : AssetFormIntent()
    data class UpdateGoalName(val name: String) : AssetFormIntent()

    // Actions
    data object LoadInitialData : AssetFormIntent()
    data class UpdateCategory(val category: InvestmentCategory) : AssetFormIntent()
    data class LoadAssetForEdit(val assetId: Long) : AssetFormIntent()
    data object Save : AssetFormIntent()
    data object ClearForm : AssetFormIntent()
    data object ResetCloseFlag : AssetFormIntent()
}

