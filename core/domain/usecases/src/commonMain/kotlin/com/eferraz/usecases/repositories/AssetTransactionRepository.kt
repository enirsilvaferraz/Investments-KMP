package com.eferraz.usecases.repositories

import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

public interface AssetTransactionRepository {
    public suspend fun upsert(model: AssetTransaction): Long
    public suspend fun delete(id: Long)
    public suspend fun getById(id: Long, holding: AssetHolding): AssetTransaction?
    public suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction>
    public suspend fun getAllByHoldingAndDateRange(holding: AssetHolding, startDate: LocalDate, endDate: LocalDate): List<AssetTransaction>
    public suspend fun getByGoalAndReferenceDate(month: YearMonth, goal: FinancialGoal): List<AssetTransaction>
}
