package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.IssuerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações CRUD na tabela issuers.
 */
@Dao
internal interface IssuerDao {

    @Insert
    suspend fun insert(issuer: IssuerEntity): Long

    @Insert
    suspend fun insertAll(issuers: List<IssuerEntity>): List<Long>

    @Query("SELECT * FROM issuers")
    fun getAll(): Flow<List<IssuerEntity>>

    @Query("SELECT * FROM issuers WHERE id = :id")
    suspend fun getById(id: Long): IssuerEntity?
}

