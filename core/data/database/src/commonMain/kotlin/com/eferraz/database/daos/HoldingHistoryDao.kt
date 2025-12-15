package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.database.entities.relationship.HoldingHistoryWithDetails
import kotlinx.datetime.YearMonth

/**
 * DAO para operações CRUD na tabela holding_history.
 */
@Dao
internal interface HoldingHistoryDao {

    @Upsert
    suspend fun upsert(historyEntry: HoldingHistoryEntryEntity): Long

    @Update
    suspend fun update(historyEntry: HoldingHistoryEntryEntity)

    @Query("SELECT * FROM holding_history")
    suspend fun getAll(): List<HoldingHistoryEntryEntity>

    @Query("SELECT * FROM holding_history WHERE id = :id")
    suspend fun getById(id: Long): HoldingHistoryEntryEntity?

    @Query("SELECT * FROM holding_history WHERE holdingId = :holdingId")
    suspend fun getByHoldingId(holdingId: Long): List<HoldingHistoryEntryEntity>

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
            asset_holdings.brokerageId AS holding_brokerageId
        FROM holding_history
        INNER JOIN asset_holdings ON holding_history.holdingId = asset_holdings.id
        WHERE holding_history.referenceDate = :referenceDate
    """
    )
    suspend fun getByReferenceDate(referenceDate: YearMonth): List<HoldingHistoryWithDetails>

    @Transaction
    @Query(
        """
        SELECT 
            holding_history.*,
            asset_holdings.id AS holding_id,
            asset_holdings.assetId AS holding_assetId,
            asset_holdings.ownerId AS holding_ownerId,
            asset_holdings.brokerageId AS holding_brokerageId
        FROM holding_history
        INNER JOIN asset_holdings ON holding_history.holdingId = asset_holdings.id
        WHERE holding_history.holdingId = :holdingId AND holding_history.referenceDate = :referenceDate
        LIMIT 1
    """
    )
    suspend fun getByHoldingAndReferenceDate(referenceDate: YearMonth, holdingId: Long): HoldingHistoryWithDetails?
}
