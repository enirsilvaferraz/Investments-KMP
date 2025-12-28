package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eferraz.database.entities.supports.IssuerEntity
/**
 * DAO para operações CRUD na tabela issuers.
 */
@Dao
internal interface IssuerDao {

    @Insert
    suspend fun insert(issuer: IssuerEntity): Long

    @Insert
    suspend fun insertAll(issuers: List<IssuerEntity>): List<Long>

    @Query("SELECT * FROM issuers ORDER BY name")
    suspend fun getAll(): List<IssuerEntity>

    @Query("SELECT * FROM issuers WHERE id = :id")
    suspend fun getById(id: Long): IssuerEntity?

    @Query("SELECT * FROM issuers WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): IssuerEntity?
}

