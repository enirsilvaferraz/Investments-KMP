package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.AssetTransactionEntity
import com.eferraz.database.entities.FixedIncomeTransactionEntity
import com.eferraz.database.entities.FundsTransactionEntity
import com.eferraz.database.entities.VariableIncomeTransactionEntity
import com.eferraz.database.entities.relationship.TransactionWithDetails
import kotlinx.datetime.LocalDate

/**
 * DAO para operações CRUD nas tabelas de transações de ativos.
 */
@Dao
internal interface AssetTransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: AssetTransactionEntity): Long

    @Insert
    suspend fun insertFixedIncome(fixedIncome: FixedIncomeTransactionEntity)

    @Insert
    suspend fun insertVariableIncome(variableIncome: VariableIncomeTransactionEntity)

    @Insert
    suspend fun insertFunds(funds: FundsTransactionEntity)

    @Upsert
    suspend fun upsertTransaction(transaction: AssetTransactionEntity): Long

    @Upsert
    suspend fun upsertFixedIncome(fixedIncome: FixedIncomeTransactionEntity)

    @Upsert
    suspend fun upsertVariableIncome(variableIncome: VariableIncomeTransactionEntity)

    @Upsert
    suspend fun upsertFunds(funds: FundsTransactionEntity)

    @Query("SELECT * FROM asset_transactions WHERE id = :id")
    suspend fun getById(id: Long): AssetTransactionEntity?

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
        endDate: LocalDate
    ): List<TransactionWithDetails>

    @Transaction
    @Query("SELECT * FROM asset_transactions WHERE id = :id LIMIT 1")
    suspend fun getByIdWithDetails(id: Long): TransactionWithDetails?
}
