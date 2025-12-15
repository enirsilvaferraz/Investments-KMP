package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.transaction.AssetTransactionEntity
import com.eferraz.database.entities.transaction.FixedIncomeTransactionEntity
import com.eferraz.database.entities.transaction.FundsTransactionEntity
import com.eferraz.database.entities.transaction.VariableIncomeTransactionEntity
import com.eferraz.database.entities.transaction.TransactionWithDetails
import kotlinx.datetime.LocalDate

/**
 * DAO para operações CRUD nas tabelas de transações de ativos.
 */
@Dao
internal interface AssetTransactionDao {

    @Upsert
    suspend fun save(transaction: AssetTransactionEntity): Long

    @Upsert
    suspend fun save(transaction: FixedIncomeTransactionEntity): Long

    @Upsert
    suspend fun save(transaction: VariableIncomeTransactionEntity): Long

    @Upsert
    suspend fun save(transaction: FundsTransactionEntity): Long

    @Query("SELECT * FROM asset_transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionWithDetails?

    @Query("DELETE FROM asset_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM asset_transactions WHERE holdingId = :holdingId ORDER BY transactionDate DESC")
    suspend fun getAllByHoldingId(holdingId: Long): List<TransactionWithDetails>

    @Transaction
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
    ): List<TransactionWithDetails>

    @Transaction
    @Query("SELECT * FROM asset_transactions WHERE id = :id LIMIT 1")
    suspend fun getByIdWithDetails(id: Long): TransactionWithDetails?
}
