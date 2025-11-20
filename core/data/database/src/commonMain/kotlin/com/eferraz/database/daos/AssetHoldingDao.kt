package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eferraz.database.entities.AssetHoldingEntity
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

    @Query("SELECT * FROM asset_holdings WHERE ownerId = :ownerId")
    fun getByOwnerId(ownerId: Long): Flow<List<AssetHoldingEntity>>

    @Query("SELECT * FROM asset_holdings WHERE brokerageId = :brokerageId")
    fun getByBrokerageId(brokerageId: Long): Flow<List<AssetHoldingEntity>>

    @Query("SELECT * FROM asset_holdings WHERE assetId = :assetId")
    fun getByAssetId(assetId: Long): Flow<List<AssetHoldingEntity>>
}

