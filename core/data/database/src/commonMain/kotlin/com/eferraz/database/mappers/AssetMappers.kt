package com.eferraz.database.mappers

import com.eferraz.database.entities.supports.IssuerEntity
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.assets.AssetWithDetails
import com.eferraz.database.entities.assets.FixedIncomeAssetEntity
import com.eferraz.database.entities.assets.InvestmentFundAssetEntity
import com.eferraz.database.entities.assets.VariableIncomeAssetEntity
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.VariableIncomeAsset

/**
 * Mappers para conversão entre entidades de domínio e entidades de banco de dados de ativos.
 */

internal fun AssetWithDetails.toDomain(): Asset {

    return when {

        fixedIncome != null -> FixedIncomeAsset(
            id = asset.id,
            issuer = issuer.toDomain(),
            type = fixedIncome.type,
            subType = fixedIncome.subType,
            expirationDate = fixedIncome.expirationDate,
            contractedYield = fixedIncome.contractedYield,
            cdiRelativeYield = fixedIncome.cdiRelativeYield,
            liquidity = asset.liquidity,
            observations = asset.observations
        )

        variableIncome != null -> VariableIncomeAsset(
            id = asset.id,
            name = asset.name,
            issuer = issuer.toDomain(),
            type = variableIncome.type,
            ticker = variableIncome.ticker,
            observations = asset.observations
        )

        funds != null -> InvestmentFundAsset(
            id = asset.id,
            name = asset.name,
            issuer = issuer.toDomain(),
            type = funds.type,
            liquidity = asset.liquidity,
            liquidityDays = funds.liquidityDays,
            expirationDate = funds.expirationDate,
            observations = asset.observations
        )

        else -> throw IllegalStateException("AssetWithDetails must have at least one specific asset type")
    }
}

private fun IssuerEntity.toDomain(): Issuer = Issuer(
    id = id, name = name, isInLiquidation = isInLiquidation
)

internal fun Issuer.toEntity() = IssuerEntity(
    id = id, name = name, isInLiquidation = isInLiquidation
)

internal fun Asset.toEntity(): AssetWithDetails {

    val (category, liquidity) = when (this) {
        is FixedIncomeAsset -> "FIXED_INCOME" to liquidity
        is VariableIncomeAsset -> "VARIABLE_INCOME" to liquidity
        is InvestmentFundAsset -> "INVESTMENT_FUND" to liquidity
    }

    val assetEntity = AssetEntity(
        id = id,
        name = name,
        issuerId = issuer.id,
        category = category,
        liquidity = liquidity,
        observations = observations
    )

    return when (this) {

        is FixedIncomeAsset -> AssetWithDetails(
            asset = assetEntity,
            issuer = issuer.toEntity(),
            fixedIncome = FixedIncomeAssetEntity(
                assetId = id,
                type = type,
                subType = subType,
                expirationDate = expirationDate,
                contractedYield = contractedYield,
                cdiRelativeYield = cdiRelativeYield
            )
        )

        is VariableIncomeAsset -> AssetWithDetails(
            asset = assetEntity,
            issuer = issuer.toEntity(),
            variableIncome = VariableIncomeAssetEntity(
                assetId = id,
                type = type,
                ticker = ticker
            )
        )

        is InvestmentFundAsset -> AssetWithDetails(
            asset = assetEntity,
            issuer = issuer.toEntity(),
            funds = InvestmentFundAssetEntity(
                assetId = id,
                type = type,
                liquidityDays = liquidityDays,
                expirationDate = expirationDate
            )
        )
    }
}

