package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.database.entities.relationship.HoldingHistoryWithDetails
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

    @Transaction
    @Query(
        """
        SELECT 
            holding_history.*,
            asset_holdings.id AS holding_id,
            asset_holdings.assetId AS holding_assetId,
            asset_holdings.ownerId AS holding_ownerId,
            asset_holdings.brokerageId AS holding_brokerageId,
            asset_holdings.quantity AS holding_quantity,
            asset_holdings.averageCost AS holding_averageCost,
            asset_holdings.investedValue AS holding_investedValue,
            asset_holdings.currentValue AS holding_currentValue
        FROM holding_history
        INNER JOIN asset_holdings ON holding_history.holdingId = asset_holdings.id
        WHERE holding_history.referenceDate = :referenceDate
    """
    )
    fun getByReferenceDate(referenceDate: YearMonth): Flow<List<HoldingHistoryWithDetails>>
}
