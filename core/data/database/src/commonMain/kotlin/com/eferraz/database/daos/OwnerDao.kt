package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.OwnerEntity
/**
 * DAO para operações CRUD na tabela owners.
 */
@Dao
internal interface OwnerDao {

    @Insert
    suspend fun insert(owner: OwnerEntity): Long

    @Insert
    suspend fun insertAll(owners: List<OwnerEntity>): List<Long>

    @Query("SELECT * FROM owners")
    suspend fun getAll(): List<OwnerEntity>

    @Query("SELECT * FROM owners WHERE id = :id")
    suspend fun getById(id: Long): OwnerEntity?
}

