package com.eferraz.database.mappers

import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.assets.AssetWithDetails
import com.eferraz.database.entities.assets.FixedIncomeAssetEntity
import com.eferraz.database.entities.assets.InvestmentFundAssetEntity
import com.eferraz.database.entities.assets.VariableIncomeAssetEntity
import com.eferraz.database.entities.supports.IssuerEntity
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.VariableIncomeAsset

/**
 * Literais persistidos em `assets.asset_class` e `asset_transactions.asset_class`.
 * Fonte única para mappers de ativos e transações (R5).
 */
internal object PersistedAssetClass {
    const val FIXED_INCOME = "FIXED_INCOME"
    const val VARIABLE_INCOME = "VARIABLE_INCOME"
    const val INVESTMENT_FUND = "INVESTMENT_FUND"
}

/**
 * Mappers para conversão entre entidades de domínio e entidades de banco de dados de ativos.
 */

internal fun AssetWithDetails.toDomain(): Asset =
    when {

        fixedIncome != null -> FixedIncomeAsset(
            id = asset.id,
            issuer = issuer.toDomain(),
            indexer = fixedIncome.indexer,
            type = fixedIncome.type,
            expirationDate = fixedIncome.expirationDate,
            contractedYield = fixedIncome.contractedYield,
            cdiRelativeYield = fixedIncome.cdiRelativeYield,
            liquidity = asset.liquidity,
            observations = asset.observations,
            b3Identifier = fixedIncome.b3Identifier,
            incomeTaxExempt = fixedIncome.incomeTaxExempt,
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

private fun IssuerEntity.toDomain(): Issuer =
    Issuer(
        id = id,
        name = name,
        isInLiquidation = isInLiquidation
    )

internal fun Issuer.toEntity() =
    IssuerEntity(
        id = id,
        name = name,
        isInLiquidation = isInLiquidation
    )

internal fun Asset.toEntity(): AssetWithDetails {

    val (assetClass, liquidity) = when (this) {
        is FixedIncomeAsset -> PersistedAssetClass.FIXED_INCOME to liquidity
        is VariableIncomeAsset -> PersistedAssetClass.VARIABLE_INCOME to liquidity
        is InvestmentFundAsset -> PersistedAssetClass.INVESTMENT_FUND to liquidity
    }

    val name = when (this) {
        is FixedIncomeAsset -> ""
        is VariableIncomeAsset -> this.name
        is InvestmentFundAsset -> this.name
    }

    val assetEntity = AssetEntity(
        id = id,
        name = name,
        issuerId = issuer.id,
        assetClass = assetClass,
        liquidity = liquidity,
        observations = observations
    )

    return when (this) {

        is FixedIncomeAsset -> AssetWithDetails(
            asset = assetEntity,
            issuer = issuer.toEntity(),
            fixedIncome = FixedIncomeAssetEntity(
                assetId = id,
                indexer = indexer,
                type = type,
                expirationDate = expirationDate,
                contractedYield = contractedYield,
                cdiRelativeYield = cdiRelativeYield,
                b3Identifier = b3Identifier?.trim()?.ifBlank { null },
                incomeTaxExempt = incomeTaxExempt,
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
