package com.eferraz.asset_management.assets

import androidx.compose.runtime.Immutable
import com.eferraz.design_system.components.segmented_control.SegmentedControlChoice
import com.eferraz.design_system.core.StableList
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.AssetType
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner

internal fun assetTypeOptionsForClass(assetClass: AssetClass): List<AssetType> = when (assetClass) {
    AssetClass.FIXED_INCOME -> FixedIncomeAssetType.entries.map { it }
    AssetClass.VARIABLE_INCOME -> VariableIncomeAssetType.entries.map { it }
    AssetClass.INVESTMENT_FUND -> InvestmentFundAssetType.entries.map { it }
}

internal fun incomeTaxSelectedFor(exempt: Boolean): SegmentedControlChoice<String> =
    if (exempt) SegmentedControlChoice("Sim", "Sim") else SegmentedControlChoice("Não", "Não")

internal val incomeTaxSegmentOptions: StableList<SegmentedControlChoice<String>> = StableList(
    listOf(SegmentedControlChoice("Sim", "Sim"), SegmentedControlChoice("Não", "Não")),
)

@Immutable
internal data class AssetManagementUiState(

    val asset: Asset? = null,
    val issuers: List<Issuer> = emptyList(),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,

    val brokerage: Brokerage? = null,
    val brokerages: List<Brokerage> = emptyList(),
    val brokerageError: String? = null,
    val holdingId: Long? = null,
    val owner: Owner? = null,

    val assetClass: AssetClass = AssetClass.FIXED_INCOME,

    val observations: String? = null,
    val b3Identifier: String? = null,
    val issuer: Issuer? = null,
    val yieldIndexer: YieldIndexer? = null,
    val type: AssetType? = null,
    val fixedExpiration: String? = null,
    val fixedYield: String? = null,
    val fixedCdi: String? = null,
    val fixedLiquidity: Liquidity? = null,
    val variableName: String? = null,
//    val variableType: VariableIncomeAssetType? = null,
    val variableTicker: String? = null,
    val variableCnpj: String? = null,
    val fundName: String? = null,
//    val fundType: InvestmentFundAssetType? = null,
    val fundLiquidity: Liquidity? = null,

    val incomeTaxExempt: Boolean = false,
    val assetTypeOptions: List<AssetType> = assetTypeOptionsForClass(AssetClass.FIXED_INCOME),
    val incomeTaxSelected: SegmentedControlChoice<String> = incomeTaxSelectedFor(false),

    val issuerError: String? = null,
    val yieldIndexerError: String? = null,
    val typeError: String? = null,
    val fixedExpirationError: String? = null,
    val fixedYieldError: String? = null,
    val fixedCdiError: String? = null,
    val fixedLiquidityError: String? = null,
//    val variableTypeError: String? = null,
    val variableTickerError: String? = null,
    val cnpjError: String? = null,
    val fundNameError: String? = null,
//    val fundTypeError: String? = null,
    val fundLiquidityError: String? = null,
) {

    /**
     * Todos os erros de campo a `null` (ex. antes de `runUpsert` ou ao trocar classe).
     */
    internal fun withClearedFieldErrors() = copy(
        issuerError = null,
        brokerageError = null,
        yieldIndexerError = null,
        typeError = null,
        fixedExpirationError = null,
        fixedYieldError = null,
        fixedCdiError = null,
        fixedLiquidityError = null,
//        variableTypeError = null,
        variableTickerError = null,
        cnpjError = null,
        fundNameError = null,
//        fundTypeError = null,
        fundLiquidityError = null,
    )

    internal fun hasAnyFieldError(): Boolean =
        issuerError != null || brokerageError != null ||
                yieldIndexerError != null || typeError != null || fixedExpirationError != null ||
                fixedYieldError != null || fixedCdiError != null || fixedLiquidityError != null || variableTickerError != null || cnpjError != null ||
                fundNameError != null || fundLiquidityError != null

    /**
     * Limpa tipo e campos específicos da classe anterior; mantém emissor, observações e posicionamento.
     */
    internal fun partialResetForAssetClass(assetClass: AssetClass): AssetManagementUiState =
        withClearedFieldErrors().copy(
            assetClass = assetClass,
            type = null,
            incomeTaxExempt = false,
            incomeTaxSelected = incomeTaxSelectedFor(false),
            assetTypeOptions = assetTypeOptionsForClass(assetClass),
            yieldIndexer = null,
            fixedExpiration = null,
            fixedYield = null,
            fixedCdi = null,
            fixedLiquidity = null,
            b3Identifier = null,
            variableName = null,
            variableTicker = null,
            variableCnpj = null,
            fundName = null,
            fundLiquidity = null,
        )

    internal fun withDerivedFields(): AssetManagementUiState = copy(
        assetTypeOptions = assetTypeOptionsForClass(assetClass),
        incomeTaxSelected = incomeTaxSelectedFor(incomeTaxExempt),
    )
}
