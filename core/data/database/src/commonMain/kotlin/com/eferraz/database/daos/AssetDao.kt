package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM assets WHERE id = :id")
    fun getAssetById(id: Long): Flow<AssetEntity>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'FIXED_INCOME'")
    fun getAllFixedIncomeAssets(): Flow<List<FixedIncomeAssetWithDetails>>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'VARIABLE_INCOME'")
    fun getAllVariableIncomeAssets(): Flow<List<VariableIncomeAssetWithDetails>>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'INVESTMENT_FUND'")
    fun getAllInvestmentFundAssets(): Flow<List<InvestmentFundAssetWithDetails>>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'FIXED_INCOME' AND id = :id")
    fun getFixedIncomeAssetById(id: Long): Flow<FixedIncomeAssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'VARIABLE_INCOME' AND id = :id")
    fun getVariableIncomeAssetById(id: Long): Flow<VariableIncomeAssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'INVESTMENT_FUND' AND id = :id")
    fun getInvestmentFundAssetById(id: Long): Flow<InvestmentFundAssetWithDetails>
}

