package com.eferraz.presentation.features.assetForm

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.entities.AssetFormData
import com.eferraz.usecases.entities.FixedIncomeFormData
import com.eferraz.usecases.entities.InvestmentFundFormData
import com.eferraz.usecases.entities.VariableIncomeFormData
import com.eferraz.usecases.repositories.AssetHoldingRepository
import org.koin.core.annotation.Factory

@Factory
internal class AssetToFormDataMapper(
    private val assetHoldingRepository: AssetHoldingRepository,
) {

    suspend fun toFormData(asset: Asset): AssetFormData {

        val assetHolding = assetHoldingRepository.getByAssetId(asset.id)
        val brokerageName = assetHolding?.brokerage?.name

        return when (asset) {

            is FixedIncomeAsset -> FixedIncomeFormData(
                id = asset.id,
                category = InvestmentCategory.FIXED_INCOME,
                type = asset.type,
                subType = asset.subType,
                expirationDate = asset.expirationDate.toString(),
                contractedYield = asset.contractedYield.toString(),
                cdiRelativeYield = asset.cdiRelativeYield?.toString(),
                liquidity = asset.liquidity,
                issuerName = asset.issuer.name,
                observations = asset.observations,
                brokerageName = brokerageName
            )

            is InvestmentFundAsset -> InvestmentFundFormData(
                id = asset.id,
                category = InvestmentCategory.INVESTMENT_FUND,
                type = asset.type,
                name = asset.name,
                liquidityDays = asset.liquidityDays.toString(),
                expirationDate = asset.expirationDate?.toString(),
                issuerName = asset.issuer.name,
                observations = asset.observations,
                brokerageName = brokerageName
            )

            is VariableIncomeAsset -> VariableIncomeFormData(
                id = asset.id,
                category = InvestmentCategory.VARIABLE_INCOME,
                type = asset.type,
                ticker = asset.ticker,
                issuerName = asset.issuer.name,
                observations = asset.observations,
                brokerageName = brokerageName
            )
        }
    }
}

