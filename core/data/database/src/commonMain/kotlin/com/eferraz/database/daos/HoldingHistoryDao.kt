package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/**
 * DAO para operações CRUD na tabela holding_history.
 */
@Dao
internal interface HoldingHistoryDao {

    @Insert
    suspend fun insert(historyEntry: HoldingHistoryEntryEntity): Long

    @Insert
    suspend fun insertAll(historyEntries: List<HoldingHistoryEntryEntity>): List<Long>

    @Update
    suspend fun update(historyEntry: HoldingHistoryEntryEntity)

    @Query("SELECT * FROM holding_history")
    fun getAll(): Flow<List<HoldingHistoryEntryEntity>>

    @Query("SELECT * FROM holding_history WHERE id = :id")
    suspend fun getById(id: Long): HoldingHistoryEntryEntity?

    @Query("SELECT * FROM holding_history WHERE holdingId = :holdingId")
    fun getByHoldingId(holdingId: Long): Flow<List<HoldingHistoryEntryEntity>>

    @Query("SELECT * FROM holding_history WHERE holdingId = :holdingId AND referenceDate = :referenceDate")
    suspend fun getByHoldingIdAndDate(holdingId: Long, referenceDate: YearMonth): HoldingHistoryEntryEntity?

    @Query("SELECT * FROM holding_history WHERE referenceDate = :referenceDate")
    fun getByReferenceDate(referenceDate: YearMonth): Flow<List<HoldingHistoryEntryEntity>>
}

