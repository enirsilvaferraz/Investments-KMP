package com.eferraz.repositories

import com.eferraz.database.datasources.AssetTransactionDataSource
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory(binds = [AssetTransactionRepository::class])
internal class AssetTransactionRepositoryImpl(
    private val dataSource: AssetTransactionDataSource,
) : AssetTransactionRepository {

    override suspend fun save(transaction: AssetTransaction): Result<Long> {
        return try {
            val id = dataSource.save(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getById(id: Long, holding: AssetHolding): AssetTransaction? {
        return dataSource.getById(id, holding)
    }

    override suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction> {
        return dataSource.getAllByHolding(holding)
    }

    override suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AssetTransaction> {
        return dataSource.getAllByHoldingAndDateRange(holding, startDate, endDate)
    }

    override suspend fun delete(id: Long): Result<Unit> {
        return try {
            dataSource.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(transaction: AssetTransaction): Result<Unit> {
        return try {
            dataSource.update(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
