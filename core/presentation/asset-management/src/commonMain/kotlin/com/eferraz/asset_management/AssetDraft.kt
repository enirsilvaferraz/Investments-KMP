package com.eferraz.asset_management

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

/**
 * Mensagens de validação alinhadas aos identificadores usados em [validateAssetDraft] (estado de formulário unido ao [AssetDraft]).
 */
@Immutable
internal data class AssetFormErrors(
    val issuer: String? = null,
    val brokerage: String? = null,
    val fixedType: String? = null,
    val fixedSubType: String? = null,
    val fixedExpiration: String? = null,
    val fixedYield: String? = null,
    val fixedCdi: String? = null,
    val fixedLiquidity: String? = null,
    val variableType: String? = null,
    val variableTicker: String? = null,
    val cnpj: String? = null,
    val fundName: String? = null,
    val fundType: String? = null,
    val fundLiquidity: String? = null,
    val fundLiquidityDays: String? = null,
    val fundExpiration: String? = null,
) {
    internal companion object {
        val Empty = AssetFormErrors()
    }
}

internal fun AssetFormErrors.hasAnyError(): Boolean = this != AssetFormErrors.Empty

/**
 * Rascunho editável do formulário (valores de campos + erros de validação) para a UI e para comparar com o estado inicial.
 */
@Immutable
internal data class AssetDraft(
    val category: InvestmentCategory = InvestmentCategory.FIXED_INCOME,
    val issuer: Issuer? = null,
    val brokerage: Brokerage? = null,
    val observations: String? = null,
    val errors: AssetFormErrors = AssetFormErrors.Empty,
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

internal fun initialAssetDraft(): AssetDraft = AssetDraft()

internal fun AssetDraft.withCategoryPreservingIssuerAndObs(category: InvestmentCategory): AssetDraft =
    initialAssetDraft().copy(
        category = category,
        issuer = issuer,
        brokerage = brokerage,
        observations = observations,
    )

internal fun Map<String, String>.toAssetFormErrors(): AssetFormErrors = AssetFormErrors(
    issuer = this["issuer"],
    brokerage = this["brokerage"],
    fixedType = this["fixedType"],
    fixedSubType = this["fixedSubType"],
    fixedExpiration = this["fixedExpiration"],
    fixedYield = this["fixedYield"],
    fixedCdi = this["fixedCdi"],
    fixedLiquidity = this["fixedLiquidity"],
    variableType = this["variableType"],
    variableTicker = this["variableTicker"],
    cnpj = this["cnpj"],
    fundName = this["fundName"],
    fundType = this["fundType"],
    fundLiquidity = this["fundLiquidity"],
    fundLiquidityDays = this["fundLiquidityDays"],
    fundExpiration = this["fundExpiration"],
)
