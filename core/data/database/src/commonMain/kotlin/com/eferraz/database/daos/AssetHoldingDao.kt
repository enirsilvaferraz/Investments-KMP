package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.database.entities.relationship.AssetHoldingWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações CRUD na tabela asset_holdings.
 */
@Dao
internal interface AssetHoldingDao {

    @Insert
    suspend fun insert(holding: AssetHoldingEntity): Long

    @Insert
    suspend fun insertAll(holdings: List<AssetHoldingEntity>): List<Long>

    @Update
    suspend fun update(holding: AssetHoldingEntity)

    @Query("SELECT * FROM asset_holdings")
    fun getAll(): Flow<List<AssetHoldingEntity>>

    @Query("SELECT * FROM asset_holdings WHERE id = :id")
    suspend fun getById(id: Long): AssetHoldingEntity?

    @Transaction
    @Query("SELECT * FROM asset_holdings")
    fun getAllWithAsset(): Flow<List<AssetHoldingWithDetails>>

//    @Transaction
//    @Query("SELECT * FROM asset_holdings WHERE id = :id")
//    suspend fun getByIdWithAsset(id: Long): AssetHoldingWithDetails?
}

