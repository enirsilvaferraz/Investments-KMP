package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.assets.FixedIncomeAssetEntity
import com.eferraz.database.entities.assets.InvestmentFundAssetEntity
import com.eferraz.database.entities.assets.VariableIncomeAssetEntity
import com.eferraz.database.entities.assets.AssetWithDetails
import com.eferraz.entities.assets.InvestmentCategory

/**
 * DAO para operações CRUD na tabela assets e suas subclasses.
 * Suporta a estrutura polimórfica "Table per Subclass".
 */
@Dao
internal interface AssetDao {

    @Upsert
    suspend fun save(asset: AssetEntity): Long

    @Upsert
    suspend fun save(asset: FixedIncomeAssetEntity): Long

    @Upsert
    suspend fun save(asset: VariableIncomeAssetEntity): Long

    @Upsert
    suspend fun save(asset: InvestmentFundAssetEntity): Long

    @Transaction
    suspend fun save(relationship: AssetWithDetails): Long {
        val id = save(relationship.asset).takeIf { it != -1L } ?: relationship.asset.id
        relationship.fixedIncome?.let { save(it.copy(assetId = id)) }
        relationship.variableIncome?.let { save(it.copy(assetId = id)) }
        relationship.funds?.let { save(it.copy(assetId = id)) }
        return id
    }

    @Transaction
    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun find(id: Long): AssetWithDetails?

    @Transaction
    @Query("SELECT * FROM assets")
    suspend fun getAll(): List<AssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = 'VARIABLE_INCOME'")
    suspend fun getAllVariableIncomeAssets(): List<AssetWithDetails>

    @Transaction
    @Query("SELECT * FROM assets WHERE category = :category")
    suspend fun getByType(category: InvestmentCategory): List<AssetWithDetails>

    @Transaction
    @Query("""
        SELECT * FROM assets 
        WHERE category = 'VARIABLE_INCOME' 
        AND id IN (
            SELECT assetId FROM variable_income_assets WHERE ticker = :ticker
        )
    """)
    suspend fun findByTicker(ticker: String): AssetWithDetails?
}
