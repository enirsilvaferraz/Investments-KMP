package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.entities.holdings.AssetHoldingWithDetails
import com.eferraz.entities.assets.InvestmentCategory
/**
 * DAO para operações CRUD na tabela asset_holdings.
 */
@Dao
internal interface AssetHoldingDao {

    @Upsert
    suspend fun upsert(holding: AssetHoldingEntity): Long

    @Insert
    suspend fun insertAll(holdings: List<AssetHoldingEntity>): List<Long>

    @Query("SELECT * FROM asset_holdings")
    suspend fun getAll(): List<AssetHoldingEntity>

    @Query("SELECT * FROM asset_holdings WHERE id = :id")
    suspend fun getById(id: Long): AssetHoldingEntity?

    @Query("SELECT * FROM asset_holdings WHERE assetId = :assetId")
    suspend fun getByAssetId(assetId: Long): AssetHoldingEntity?

    @Query("DELETE FROM asset_holdings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM asset_holdings")
    suspend fun getAllWithAsset(): List<AssetHoldingWithDetails>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT * FROM asset_holdings
        INNER JOIN assets ON asset_holdings.assetId = assets.id
        WHERE assets.category = :category
    """)
    suspend fun getAllWithAssetByCategory(category: InvestmentCategory): List<AssetHoldingWithDetails>

    @Transaction
    @Query("SELECT * FROM asset_holdings WHERE goalId = :goalId")
    suspend fun getAllWithAssetByGoalId(goalId: Long): List<AssetHoldingWithDetails>
}

