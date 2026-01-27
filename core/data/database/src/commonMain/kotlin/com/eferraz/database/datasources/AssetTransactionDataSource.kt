package com.eferraz.database.datasources

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import kotlinx.datetime.LocalDate

public interface AssetTransactionDataSource {
    public suspend fun save(transaction: AssetTransaction): Long
    public suspend fun find(id: Long, holding: AssetHolding): AssetTransaction?
    public suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction>
    public suspend fun getAllByHoldingAndDateRange(holding: AssetHolding, startDate: LocalDate, endDate: LocalDate): List<AssetTransaction>
    public suspend fun delete(id: Long)
}
