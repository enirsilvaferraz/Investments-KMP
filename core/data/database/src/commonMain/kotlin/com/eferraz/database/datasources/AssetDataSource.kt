package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetDao
import com.eferraz.database.relationship.FixedIncomeAssetWithDetails
import com.eferraz.database.relationship.InvestmentFundAssetWithDetails
import com.eferraz.database.relationship.VariableIncomeAssetWithDetails
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.Issuer
import com.eferraz.entities.VariableIncomeAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

public interface AssetDataSource {
    public fun getAll(): Flow<List<Asset>>
}

@Factory(binds = [AssetDataSource::class])
internal class AssetDataSourceImpl(
    private val assetDao: AssetDao,
) : AssetDataSource {

    override fun getAll(): Flow<List<Asset>> {
        return combine(
            assetDao.getAllFixedIncomeAssets(),
            assetDao.getAllVariableIncomeAssets(),
            assetDao.getAllInvestmentFundAssets()
        ) { fixedIncome, variableIncome, investmentFund ->
            buildList {
                addAll(fixedIncome.map { it.toFixedIncomeAsset() })
                addAll(variableIncome.map { it.toVariableIncomeAsset() })
                addAll(investmentFund.map { it.toInvestmentFundAsset() })
            }
        }
    }

    private fun FixedIncomeAssetWithDetails.toFixedIncomeAsset(): FixedIncomeAsset {
        return FixedIncomeAsset(
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
    }

    private fun VariableIncomeAssetWithDetails.toVariableIncomeAsset(): VariableIncomeAsset {
        return VariableIncomeAsset(
            id = asset.id,
            name = asset.name,
            issuer = Issuer(id = issuer.id, name = issuer.name),
            type = variableIncome.type,
            ticker = variableIncome.ticker,
            liquidity = asset.liquidity,
            observations = asset.observations
        )
    }

    private fun InvestmentFundAssetWithDetails.toInvestmentFundAsset(): InvestmentFundAsset {
        return InvestmentFundAsset(
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
}
