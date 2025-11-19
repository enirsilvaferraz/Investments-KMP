package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.relationship.FixedIncomeAssetWithDetails
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.relationship.InvestmentFundAssetWithDetails
import com.eferraz.database.entities.VariableIncomeAssetEntity
import com.eferraz.database.entities.relationship.VariableIncomeAssetWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações CRUD na tabela assets e suas subclasses.
 * Suporta a estrutura polimórfica "Table per Subclass".
 */
@Dao
internal interface AssetDao {

    @Insert
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert
    suspend fun insertFixedIncome(fixedIncome: FixedIncomeAssetEntity)

    @Insert
    suspend fun insertVariableIncome(variableIncome: VariableIncomeAssetEntity)

    @Insert
    suspend fun insertInvestmentFund(investmentFund: InvestmentFundAssetEntity)

    @Query("SELECT * FROM assets")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Query(
        """
        SELECT 
            assets.id,
            assets.name,
            assets.issuerId,
            assets.category,
            assets.liquidity,
            assets.observations,
            issuers.id AS issuer_id,
            issuers.name AS issuer_name,
            fixed_income_assets.assetId AS fixed_income_assetId,
            fixed_income_assets.type AS fixed_income_type,
            fixed_income_assets.subType AS fixed_income_subType,
            fixed_income_assets.expirationDate AS fixed_income_expirationDate,
            fixed_income_assets.contractedYield AS fixed_income_contractedYield,
            fixed_income_assets.cdiRelativeYield AS fixed_income_cdiRelativeYield
        FROM assets
        INNER JOIN issuers ON assets.issuerId = issuers.id
        INNER JOIN fixed_income_assets ON assets.id = fixed_income_assets.assetId
        WHERE assets.category = 'FIXED_INCOME'
        """
    )
    fun getAllFixedIncomeAssets(): Flow<List<FixedIncomeAssetWithDetails>>

    @Query(
        """
        SELECT 
            assets.id,
            assets.name,
            assets.issuerId,
            assets.category,
            assets.liquidity,
            assets.observations,
            issuers.id AS issuer_id,
            issuers.name AS issuer_name,
            variable_income_assets.assetId AS variable_income_assetId,
            variable_income_assets.type AS variable_income_type,
            variable_income_assets.ticker AS variable_income_ticker
        FROM assets
        INNER JOIN issuers ON assets.issuerId = issuers.id
        INNER JOIN variable_income_assets ON assets.id = variable_income_assets.assetId
        WHERE assets.category = 'VARIABLE_INCOME'
        """
    )
    fun getAllVariableIncomeAssets(): Flow<List<VariableIncomeAssetWithDetails>>

    @Query(
        """
        SELECT 
            assets.id,
            assets.name,
            assets.issuerId,
            assets.category,
            assets.liquidity,
            assets.observations,
            issuers.id AS issuer_id,
            issuers.name AS issuer_name,
            investment_fund_assets.assetId AS investment_fund_assetId,
            investment_fund_assets.type AS investment_fund_type,
            investment_fund_assets.liquidityDays AS investment_fund_liquidityDays,
            investment_fund_assets.expirationDate AS investment_fund_expirationDate
        FROM assets
        INNER JOIN issuers ON assets.issuerId = issuers.id
        INNER JOIN investment_fund_assets ON assets.id = investment_fund_assets.assetId
        WHERE assets.category = 'INVESTMENT_FUND'
        """
    )
    fun getAllInvestmentFundAssets(): Flow<List<InvestmentFundAssetWithDetails>>
}

