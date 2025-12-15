package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity
import com.eferraz.database.entities.relationship.AssetWithDetails
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.Issuer
import com.eferraz.entities.VariableIncomeAsset
import org.koin.core.annotation.Factory

@Factory(binds = [AssetDataSource::class])
internal class AssetDataSourceImpl(
    private val assetDao: AssetDao,
) : AssetDataSource {

    override suspend fun getAll(): List<Asset> {
        return assetDao.getAll().map { it.toModel() }
    }

    override suspend fun getByID(id: Long): Asset? {
        return assetDao.find(id)?.toModel()
    }

    override suspend fun save(asset: FixedIncomeAsset): Long {
        val (assetEntity, fixedIncomeEntity) = asset.toEntity()
        val insertResult = assetDao.save(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.save(fixedIncomeEntity.copy(assetId = assetId))
        return assetId
    }

    override suspend fun save(asset: InvestmentFundAsset): Long {
        val (assetEntity, investmentFundEntity) = asset.toEntity()
        val insertResult = assetDao.save(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.save(investmentFundEntity.copy(assetId = assetId))
        return assetId
    }

    override suspend fun save(asset: VariableIncomeAsset): Long {
        val (assetEntity, variableIncomeEntity) = asset.toEntity()
        val insertResult = assetDao.save(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.save(variableIncomeEntity.copy(assetId = assetId))
        return assetId
    }

    private fun AssetWithDetails.toModel(): Asset {

        return when {

            fixedIncome != null -> FixedIncomeAsset(
                id = asset.id,
                issuer = Issuer(id = issuer.id, name = issuer.name, isInLiquidation = issuer.isInLiquidation),
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
                issuer = Issuer(id = issuer.id, name = issuer.name, isInLiquidation = issuer.isInLiquidation),
                type = variableIncome.type,
                ticker = variableIncome.ticker,
                observations = asset.observations
            )

            funds != null -> InvestmentFundAsset(
                id = asset.id,
                name = asset.name,
                issuer = Issuer(id = issuer.id, name = issuer.name, isInLiquidation = issuer.isInLiquidation),
                type = funds.type,
                liquidity = asset.liquidity,
                liquidityDays = funds.liquidityDays,
                expirationDate = funds.expirationDate,
                observations = asset.observations
            )

            else -> throw IllegalStateException("AssetEntity must have at least one specific asset type")
        }
    }

    private fun FixedIncomeAsset.toEntity(): Pair<AssetEntity, FixedIncomeAssetEntity> {
        val assetEntity = AssetEntity(
            id = id,
            name = name,
            issuerId = issuer.id,
            category = "FIXED_INCOME",
            liquidity = liquidity,
            observations = observations
        )
        val fixedIncomeEntity = FixedIncomeAssetEntity(
            assetId = id,
            type = type,
            subType = subType,
            expirationDate = expirationDate,
            contractedYield = contractedYield,
            cdiRelativeYield = cdiRelativeYield
        )
        return Pair(assetEntity, fixedIncomeEntity)
    }

    private fun InvestmentFundAsset.toEntity(): Pair<AssetEntity, InvestmentFundAssetEntity> {
        val assetEntity = AssetEntity(
            id = id,
            name = name,
            issuerId = issuer.id,
            category = "INVESTMENT_FUND",
            liquidity = liquidity,
            observations = observations
        )
        val investmentFundEntity = InvestmentFundAssetEntity(
            assetId = id,
            type = type,
            liquidityDays = liquidityDays,
            expirationDate = expirationDate
        )
        return Pair(assetEntity, investmentFundEntity)
    }

    private fun VariableIncomeAsset.toEntity(): Pair<AssetEntity, VariableIncomeAssetEntity> {
        val assetEntity = AssetEntity(
            id = id,
            name = name,
            issuerId = issuer.id,
            category = "VARIABLE_INCOME",
            liquidity = liquidity,
            observations = observations
        )
        val variableIncomeEntity = VariableIncomeAssetEntity(
            assetId = id,
            type = type,
            ticker = ticker
        )
        return Pair(assetEntity, variableIncomeEntity)
    }
}