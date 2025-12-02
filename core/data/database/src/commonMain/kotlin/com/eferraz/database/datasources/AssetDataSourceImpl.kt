package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
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

    override suspend fun getByID(id: Long): Asset {
        val assetEntity = assetDao.getAssetById(id)
            ?: throw IllegalArgumentException("Asset with id $id not found")
        
        return when (assetEntity.category) {
            "FIXED_INCOME" -> assetDao.getFixedIncomeAssetById(id)?.toModel() ?: throw IllegalArgumentException("Fixed income asset with id $id not found")
            "VARIABLE_INCOME" -> assetDao.getVariableIncomeAssetById(id)?.toModel() ?: throw IllegalArgumentException("Variable income asset with id $id not found")
            "INVESTMENT_FUND" -> assetDao.getInvestmentFundAssetById(id)?.toModel() ?: throw IllegalArgumentException("Investment fund asset with id $id not found")
            else -> throw IllegalArgumentException("Invalid asset category: ${assetEntity.category}")
        }
    }

    private fun FixedIncomeAssetWithDetails.toModel() =
        FixedIncomeAsset(
            id = asset.id,
            issuer = Issuer(id = issuer.id, name = issuer.name),
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
            issuer = Issuer(id = issuer.id, name = issuer.name),
            type = variableIncome.type,
            ticker = variableIncome.ticker,
            observations = asset.observations
        )

    private fun InvestmentFundAssetWithDetails.toModel() =
        InvestmentFundAsset(
            id = asset.id,
            name = asset.name,
            issuer = Issuer(id = issuer.id, name = issuer.name),
            type = investmentFund.type,
            liquidity = asset.liquidity,
            liquidityDays = investmentFund.liquidityDays,
            expirationDate = investmentFund.expirationDate,
            observations = asset.observations
        )
}