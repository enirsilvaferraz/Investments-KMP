package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetTransactionDao
import com.eferraz.database.entities.FixedIncomeTransactionEntity
import com.eferraz.database.entities.FundsTransactionEntity
import com.eferraz.database.entities.VariableIncomeTransactionEntity
import com.eferraz.database.mappers.toDomain
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory(binds = [AssetTransactionDataSource::class])
internal class AssetTransactionDataSourceImpl(
    private val assetTransactionDao: AssetTransactionDao,
) : AssetTransactionDataSource {

    override suspend fun save(transaction: AssetTransaction): Long {
        val (baseEntity, specificEntity) = transaction.toEntity()
        
        val transactionId = if (transaction.id > 0) {
            // Atualização
            assetTransactionDao.save(baseEntity)
            transaction.id
        } else {
            // Inserção - o ID é gerado pelo banco
            assetTransactionDao.save(baseEntity)
        }

        when (specificEntity) {
            is FixedIncomeTransactionEntity -> {
                val fixedIncomeEntity = specificEntity.copy(transactionId = transactionId)
                if (transaction.id > 0) {
                    assetTransactionDao.save(fixedIncomeEntity)
                } else {
                    assetTransactionDao.save(fixedIncomeEntity)
                }
            }
            is VariableIncomeTransactionEntity -> {
                val variableIncomeEntity = specificEntity.copy(transactionId = transactionId)
                if (transaction.id > 0) {
                    assetTransactionDao.save(variableIncomeEntity)
                } else {
                    assetTransactionDao.save(variableIncomeEntity)
                }
            }
            is FundsTransactionEntity -> {
                val fundsEntity = specificEntity.copy(transactionId = transactionId)
                if (transaction.id > 0) {
                    assetTransactionDao.save(fundsEntity)
                } else {
                    assetTransactionDao.save(fundsEntity)
                }
            }
        }

        return transactionId
    }

    override suspend fun getById(id: Long, holding: AssetHolding): AssetTransaction? {
        val transactionWithDetails = assetTransactionDao.getByIdWithDetails(id) ?: return null
        return transactionWithDetails.toDomain(holding)
    }

    override suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction> {
        val transactionsWithDetails = assetTransactionDao.getAllByHoldingId(holding.id)
        return transactionsWithDetails.map { it.toDomain(holding) }
    }

    override suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AssetTransaction> {
        val transactionsWithDetails = assetTransactionDao.getAllByHoldingIdAndDateRange(
            holdingId = holding.id,
            startDate = startDate,
            endDate = endDate
        )
        return transactionsWithDetails.map { it.toDomain(holding) }
    }

    override suspend fun delete(id: Long) {
        assetTransactionDao.deleteById(id)
    }

    override suspend fun update(transaction: AssetTransaction) {
        save(transaction)
    }
}
