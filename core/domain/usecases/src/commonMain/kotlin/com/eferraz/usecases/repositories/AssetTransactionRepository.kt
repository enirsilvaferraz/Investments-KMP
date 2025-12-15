package com.eferraz.usecases.repositories

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import kotlinx.datetime.LocalDate

public interface AssetTransactionRepository {
    public suspend fun save(transaction: AssetTransaction): Long
    public suspend fun delete(id: Long)
    public suspend fun getById(id: Long, holding: AssetHolding): AssetTransaction?
    public suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction>
    public suspend fun getAllByHoldingAndDateRange(holding: AssetHolding, startDate: LocalDate, endDate: LocalDate): List<AssetTransaction>
}
