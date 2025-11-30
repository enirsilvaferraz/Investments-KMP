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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory(binds = [AssetDataSource::class])
internal class AssetDataSourceImpl(
    private val assetDao: AssetDao,
) : AssetDataSource {

    override fun getAll() = combine(
        assetDao.getAllFixedIncomeAssets(),
        assetDao.getAllVariableIncomeAssets(),
        assetDao.getAllInvestmentFundAssets()
    ) { fixedIncome, variableIncome, investmentFund ->
        buildList {
            addAll(fixedIncome.map { it.toModel() })
            addAll(variableIncome.map { it.toModel() })
            addAll(investmentFund.map { it.toModel() })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByID(id: Long): Flow<Asset> =
        assetDao.getAssetById(id).flatMapLatest { assetEntity ->
            when (assetEntity.category) {
                "FIXED_INCOME" -> assetDao.getFixedIncomeAssetById(id).map { it.toModel() }
                "VARIABLE_INCOME" -> assetDao.getVariableIncomeAssetById(id).map { it.toModel() }
                "INVESTMENT_FUND" -> assetDao.getInvestmentFundAssetById(id).map { it.toModel() }
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