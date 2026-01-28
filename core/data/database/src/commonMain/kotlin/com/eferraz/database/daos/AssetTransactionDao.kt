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

    @Transaction
    suspend fun save(relationship: TransactionWithDetails): Long {
        val id = save(relationship.transaction).takeIf { it != -1L } ?: relationship.transaction.id
        relationship.fixedIncome?.let { save(it.copy(transactionId = id)) }
        relationship.variableIncome?.let { save(it.copy(transactionId = id)) }
        relationship.funds?.let { save(it.copy(transactionId = id)) }
        return id
    }

    @Transaction
    @Query("SELECT * FROM asset_transactions WHERE id = :id")
    suspend fun find(id: Long): TransactionWithDetails?

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
    suspend fun getByGoalAndDateRange(goalId: Long, startDate: LocalDate, endDate: LocalDate): List<TransactionWithDetails>
}
