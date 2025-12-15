package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.supports.BrokerageEntity
/**
 * DAO para operações CRUD na tabela brokerages.
 */
@Dao
internal interface BrokerageDao {

    @Insert
    suspend fun insert(brokerage: BrokerageEntity): Long

    @Insert
    suspend fun insertAll(brokerages: List<BrokerageEntity>): List<Long>

    @Query("SELECT * FROM brokerages")
    suspend fun getAll(): List<BrokerageEntity>

    @Query("SELECT * FROM brokerages WHERE id = :id")
    suspend fun getById(id: Long): BrokerageEntity?
}

