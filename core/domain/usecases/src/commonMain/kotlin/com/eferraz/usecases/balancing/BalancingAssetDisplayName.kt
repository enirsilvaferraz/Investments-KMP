package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer

internal fun formatBalancingAssetDisplayName(asset: Asset): String = when (asset) {
    is FixedIncomeAsset -> asset.formattedDisplayName()
    is VariableIncomeAsset -> asset.formattedDisplayName()
    is InvestmentFundAsset -> asset.formattedDisplayName()
}

private fun FixedIncomeAsset.formattedDisplayName(): String = when (indexer) {
    YieldIndexer.POST_FIXED -> "${type.name} de $contractedYield% do CDI"
    YieldIndexer.PRE_FIXED -> "${type.name} de $contractedYield% a.a."
    YieldIndexer.INFLATION_LINKED -> "${type.name} + $contractedYield%"
}

private fun VariableIncomeAsset.formattedDisplayName(): String {
    val typeLabel = when (type) {
        VariableIncomeAssetType.NATIONAL_STOCK -> "Ação Nac"
        VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ação Int"
        VariableIncomeAssetType.REAL_ESTATE_FUND -> "FII"
        VariableIncomeAssetType.ETF -> "ETF"
    }
    return "$typeLabel - $ticker"
}

private fun InvestmentFundAsset.formattedDisplayName(): String {
    val typeLabel = when (type) {
        InvestmentFundAssetType.PENSION -> "Previdência"
        InvestmentFundAssetType.STOCK_FUND -> "Fundo de Ação"
        InvestmentFundAssetType.MULTIMARKET_FUND -> "Multimercado"
    }
    return "$typeLabel - $name"
}
