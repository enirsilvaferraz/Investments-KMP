package com.eferraz.presentation.features.history

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.presentation.helpers.Formatters.formated

// Presentation Extensions for Assets

internal fun FixedIncomeAsset.displayName(): String {
    return when (type) {
        FixedIncomeAssetType.POST_FIXED -> "${subType.name} de ${contractedYield}% do CDI"
        FixedIncomeAssetType.PRE_FIXED -> "${subType.name} de ${contractedYield}% a.a."
        FixedIncomeAssetType.INFLATION_LINKED -> "${subType.name} de IPCA + ${contractedYield}%"
    }
}

internal fun Asset.displayCategory(): String = formated()

internal fun Asset.displaySubCategory(): String {
    return when (this) {
        is FixedIncomeAsset -> subType.name
        is InvestmentFundAsset -> type.formated()
        is VariableIncomeAsset -> type.formated()
        else -> ""
    }
}

internal fun Asset.displayName(): String {
    return when (this) {
        is FixedIncomeAsset -> displayName()
        is InvestmentFundAsset -> name
        is VariableIncomeAsset -> name
        else -> ""
    }
}

internal fun Asset.toInvestmentCategory(): InvestmentCategory {
    return when (this) {
        is FixedIncomeAsset -> InvestmentCategory.FIXED_INCOME
        is VariableIncomeAsset -> InvestmentCategory.VARIABLE_INCOME
        is InvestmentFundAsset -> InvestmentCategory.INVESTMENT_FUND
    }
}