package com.eferraz.presentation.features.assets

import androidx.compose.runtime.Composable
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.features.assets.Formatters.formated
import kotlinx.datetime.LocalDate

internal class AssetView(
    val category: String,
    val subCategory: String,
    val name: String,
    val maturity: LocalDate?,
    val issuer: String,
    val notes: String,
) {

    companion object {

        @Composable
        fun create(asset: Asset) = AssetView(
            category = asset.formated(),
            subCategory = when (asset) {
                is FixedIncomeAsset -> asset.subType.name
                is InvestmentFundAsset -> asset.type.formated()
                is VariableIncomeAsset -> asset.type.formated()
            },
            name = when (asset) {
                is FixedIncomeAsset -> when (asset.type) {
                    FixedIncomeAssetType.POST_FIXED -> "${asset.subType.name} de ${asset.contractedYield}% do CDI"
                    FixedIncomeAssetType.PRE_FIXED -> "${asset.subType.name} de ${asset.contractedYield}% a.a."
                    FixedIncomeAssetType.INFLATION_LINKED -> "${asset.subType.name} de IPCA + ${asset.contractedYield}%"
                }
                is InvestmentFundAsset -> asset.name
                is VariableIncomeAsset -> asset.name
            },
            maturity = when (asset) {
                is FixedIncomeAsset -> asset.expirationDate
                is InvestmentFundAsset -> asset.expirationDate
                is VariableIncomeAsset -> null
            },
            issuer = asset.issuer.name,
            notes = asset.observations.orEmpty(),
        )
    }
}