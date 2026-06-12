package com.eferraz.usecases.repositories

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction

public interface AssetTransactionRepository {
    public suspend fun upsert(holding: AssetHolding, transaction: AssetTransaction): Long
    public suspend fun delete(holding: AssetHolding, id: Long)
    public suspend fun getById(holding: AssetHolding, id: Long): AssetTransaction?
    public suspend fun saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>)
}
