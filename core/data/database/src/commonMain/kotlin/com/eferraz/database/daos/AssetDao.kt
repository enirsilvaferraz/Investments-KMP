package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity
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
}

