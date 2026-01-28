package com.eferraz.database.datasources

import com.eferraz.database.daos.AssetTransactionDao
import com.eferraz.database.mappers.toDomain
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory(binds = [AssetTransactionDataSource::class])
internal class AssetTransactionDataSourceImpl(
    private val assetTransactionDao: AssetTransactionDao,
    private val assetHoldingDataSource: AssetHoldingDataSource,
) : AssetTransactionDataSource {

    override suspend fun save(transaction: AssetTransaction): Long =
        assetTransactionDao.save(transaction.toEntity())

    override suspend fun find(id: Long, holding: AssetHolding): AssetTransaction? {
        val transactionWithDetails = assetTransactionDao.find(id) ?: return null
        return transactionWithDetails.toDomain(holding)
    }

    override suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction> {
        val transactionsWithDetails = assetTransactionDao.getAllByHoldingId(holding.id)
        return transactionsWithDetails.map { it.toDomain(holding) }
    }

    override suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AssetTransaction> {
        val transactionsWithDetails = assetTransactionDao.getAllByHoldingIdAndDateRange(
            holdingId = holding.id,
            startDate = startDate,
            endDate = endDate
        )
        return transactionsWithDetails.map { it.toDomain(holding) }
    }

    override suspend fun getByGoalAndReferenceDate(goalId: Long, startDate: LocalDate, endDate: LocalDate) =
        assetTransactionDao.getByGoalAndDateRange(
            goalId = goalId,
            startDate = startDate,
            endDate = endDate
        ).map {
            it.toDomain(assetHoldingDataSource.getById(it.transaction.holdingId))
        }

    override suspend fun delete(id: Long) {
        assetTransactionDao.deleteById(id)
    }
}
