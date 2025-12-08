package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.database.entities.relationship.AssetHoldingWithDetails
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
}

