package com.eferraz.database.daos

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.eferraz.database.entities.transaction.AssetTransactionEntity
import kotlinx.datetime.LocalDate

/**
 * DAO para operações CRUD na tabela achatada asset_transactions.
 */
@Dao
internal interface AssetTransactionDao {

    @Upsert
    suspend fun save(transaction: AssetTransactionEntity): Long

    @Query("SELECT * FROM asset_transactions WHERE id = :id")
    suspend fun find(id: Long): AssetTransactionEntity?

    @Query("DELETE FROM asset_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM asset_transactions WHERE holdingId = :holdingId ORDER BY transactionDate DESC")
    suspend fun getAllByHoldingId(holdingId: Long): List<AssetTransactionEntity>

    @Query(
        """
        SELECT * FROM asset_transactions 
        WHERE holdingId = :holdingId 
        AND transactionDate >= :startDate 
        AND transactionDate <= :endDate
        ORDER BY transactionDate DESC
    """
    )
    suspend fun getAllByHoldingIdAndDateRange(
        holdingId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AssetTransactionEntity>

    @Query(
        """
        SELECT asset_transactions.* 
        FROM asset_transactions 
        INNER JOIN asset_holdings ON asset_transactions.holdingId = asset_holdings.id
        WHERE asset_holdings.goalId = :goalId 
        AND asset_transactions.transactionDate >= :startDate 
        AND asset_transactions.transactionDate <= :endDate
        ORDER BY asset_transactions.transactionDate DESC
    """
    )
    suspend fun getByGoalAndDateRange(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AssetTransactionEntity>

    @Query(
        """
        SELECT * FROM asset_transactions 
        WHERE transactionDate >= :startDate 
        AND transactionDate <= :endDate
        ORDER BY transactionDate DESC
    """
    )
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<AssetTransactionEntity>
}
