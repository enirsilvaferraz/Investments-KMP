package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity
import com.eferraz.database.entities.relationship.FixedIncomeAssetWithDetails
import com.eferraz.database.entities.relationship.InvestmentFundAssetWithDetails
import com.eferraz.database.entities.relationship.VariableIncomeAssetWithDetails

/**
 * DAO para operações CRUD na tabela assets e suas subclasses.
 * Suporta a estrutura polimórfica "Table per Subclass".
 */
@Dao
internal interface AssetDao {

    @Upsert
    suspend fun insertAsset(asset: AssetEntity): Long

    @Upsert
    suspend fun insertFixedIncome(fixedIncome: FixedIncomeAssetEntity)

    @Upsert
    suspend fun insertVariableIncome(variableIncome: VariableIncomeAssetEntity)

    @Upsert
    suspend fun insertInvestmentFund(investmentFund: InvestmentFundAssetEntity)

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'FIXED_INCOME'")
    suspend fun getAllFixedIncomeAssets(): List<FixedIncomeAssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'VARIABLE_INCOME'")
    suspend fun getAllVariableIncomeAssets(): List<VariableIncomeAssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'INVESTMENT_FUND'")
    suspend fun getAllInvestmentFundAssets(): List<InvestmentFundAssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'FIXED_INCOME' AND id = :id")
    suspend fun getFixedIncomeAssetById(id: Long): FixedIncomeAssetWithDetails?

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'VARIABLE_INCOME' AND id = :id")
    suspend fun getVariableIncomeAssetById(id: Long): VariableIncomeAssetWithDetails?

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'INVESTMENT_FUND' AND id = :id")
    suspend fun getInvestmentFundAssetById(id: Long): InvestmentFundAssetWithDetails?
}

