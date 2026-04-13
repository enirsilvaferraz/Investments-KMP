package com.eferraz.asset_management

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType

/**
 * Rascunho editável do formulário (strings + enums) para a UI e para comparar com o estado inicial.
 */
@Immutable
internal data class AssetDraft(
    val category: InvestmentCategory = InvestmentCategory.FIXED_INCOME,
    val issuerId: Long? = null,
    val brokerageId: Long? = null,
    val observations: String? = null,
    val fixedType: FixedIncomeAssetType? = null,
    val fixedSubType: FixedIncomeSubType? = null,
    val fixedExpiration: String? = null,
    val fixedYield: String? = null,
    val fixedCdi: String? = null,
    val fixedLiquidity: Liquidity? = null,
    val variableName: String? = null,
    val variableType: VariableIncomeAssetType? = null,
    val variableTicker: String? = null,
    val variableCnpj: String? = null,
    val fundName: String? = null,
    val fundType: InvestmentFundAssetType? = null,
    val fundLiquidity: Liquidity? = null,
    val fundLiquidityDays: String? = null,
    val fundExpiration: String? = null,
)

internal fun initialAssetDraft(): AssetDraft =
    AssetDraft()

internal fun AssetDraft.withCategoryPreservingIssuerAndObs(category: InvestmentCategory): AssetDraft =
    initialAssetDraft().copy(
        category = category,
        issuerId = issuerId,
        brokerageId = brokerageId,
        observations = observations,
    )
