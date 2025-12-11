package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity
import com.eferraz.database.entities.relationship.FixedIncomeAssetWithDetails
import com.eferraz.database.entities.relationship.InvestmentFundAssetWithDetails
import com.eferraz.database.entities.relationship.VariableIncomeAssetWithDetails
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
        val fixedIncome = assetDao.getAllFixedIncomeAssets()
        val variableIncome = assetDao.getAllVariableIncomeAssets()
        val investmentFund = assetDao.getAllInvestmentFundAssets()
        
        return buildList {
            addAll(fixedIncome.map { it.toModel() })
            addAll(variableIncome.map { it.toModel() })
            addAll(investmentFund.map { it.toModel() })
        }
    }

    override suspend fun getByID(id: Long): Asset? {
        val assetEntity = assetDao.getAssetById(id) ?: return null
        
        return when (assetEntity.category) {
            "FIXED_INCOME" -> assetDao.getFixedIncomeAssetById(id)?.toModel()
            "VARIABLE_INCOME" -> assetDao.getVariableIncomeAssetById(id)?.toModel()
            "INVESTMENT_FUND" -> assetDao.getInvestmentFundAssetById(id)?.toModel()
            else -> null
        }
    }

    override suspend fun save(asset: FixedIncomeAsset): Long {
        val (assetEntity, fixedIncomeEntity) = asset.toEntity()
        val insertResult = assetDao.insertAsset(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.insertFixedIncome(fixedIncomeEntity.copy(assetId = assetId))
        return assetId
    }

    override suspend fun save(asset: InvestmentFundAsset): Long {
        val (assetEntity, investmentFundEntity) = asset.toEntity()
        val insertResult = assetDao.insertAsset(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.insertInvestmentFund(investmentFundEntity.copy(assetId = assetId))
        return assetId
    }

    override suspend fun save(asset: VariableIncomeAsset): Long {
        val (assetEntity, variableIncomeEntity) = asset.toEntity()
        val insertResult = assetDao.insertAsset(assetEntity)
        val assetId = if (insertResult == -1L) assetEntity.id else insertResult
        assetDao.insertVariableIncome(variableIncomeEntity.copy(assetId = assetId))
        return assetId
    }

    private fun FixedIncomeAssetWithDetails.toModel() =
        FixedIncomeAsset(
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

    private fun VariableIncomeAssetWithDetails.toModel() =
        VariableIncomeAsset(
            id = asset.id,
            name = asset.name,
            issuer = Issuer(id = issuer.id, name = issuer.name, isInLiquidation = issuer.isInLiquidation),
            type = variableIncome.type,
            ticker = variableIncome.ticker,
            observations = asset.observations
        )

    private fun InvestmentFundAssetWithDetails.toModel() =
        InvestmentFundAsset(
            id = asset.id,
            name = asset.name,
            issuer = Issuer(id = issuer.id, name = issuer.name, isInLiquidation = issuer.isInLiquidation),
            type = investmentFund.type,
            liquidity = asset.liquidity,
            liquidityDays = investmentFund.liquidityDays,
            expirationDate = investmentFund.expirationDate,
            observations = asset.observations
        )

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