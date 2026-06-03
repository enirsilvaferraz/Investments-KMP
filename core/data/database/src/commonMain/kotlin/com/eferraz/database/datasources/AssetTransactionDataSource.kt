package com.eferraz.database.datasources

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import kotlinx.datetime.LocalDate

public interface AssetTransactionDataSource {
    public suspend fun save(holding: AssetHolding, transaction: AssetTransaction): Long
    public suspend fun find(id: Long, holdingId: Long): AssetTransaction?
    public suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction>
    public suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AssetTransaction>
    public suspend fun getByGoalAndReferenceDate(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AssetTransaction>
    public suspend fun delete(holdingId: Long, id: Long)
    public suspend fun getByReferenceDate(startDate: LocalDate, endDate: LocalDate): List<AssetTransaction>
}
