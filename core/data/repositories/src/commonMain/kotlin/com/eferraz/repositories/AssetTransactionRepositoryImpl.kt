package com.eferraz.repositories

import com.eferraz.database.datasources.AssetTransactionDataSource
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import org.koin.core.annotation.Factory

@Factory(binds = [AssetTransactionRepository::class])
internal class AssetTransactionRepositoryImpl(
    private val dataSource: AssetTransactionDataSource,
) : AssetTransactionRepository {

    override suspend fun upsert(holding: AssetHolding, transaction: AssetTransaction): Long =
        dataSource.save(holding, transaction)

    override suspend fun getById(holding: AssetHolding, id: Long): AssetTransaction? =
        dataSource.find(id, holding.id)

    override suspend fun delete(holding: AssetHolding, id: Long) {
        dataSource.delete(holding.id, id)
    }

    override suspend fun saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>) =
        dataSource.saveAll(entries)
}
