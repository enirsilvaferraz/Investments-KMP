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

    override suspend fun save(transaction: AssetTransaction): Long =
        dataSource.save(transaction)

    override suspend fun getById(id: Long, holding: AssetHolding): AssetTransaction? =
        dataSource.find(id, holding)

    override suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction> =
        dataSource.getAllByHolding(holding)

    override suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AssetTransaction> =
        dataSource.getAllByHoldingAndDateRange(holding, startDate, endDate)

    override suspend fun delete(id: Long) {
        dataSource.delete(id)
    }
}
