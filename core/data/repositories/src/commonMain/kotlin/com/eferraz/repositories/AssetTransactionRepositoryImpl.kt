package com.eferraz.repositories

import com.eferraz.database.datasources.AssetTransactionDataSource
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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

    override suspend fun getByGoalAndReferenceDate(month: YearMonth, goal: FinancialGoal): List<AssetTransaction> {
        val startDate = LocalDate(month.year, month.month, 1)
        return dataSource.getByGoalAndReferenceDate(
            goalId = goal.id,
            startDate = startDate,
            endDate = startDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        )
    }

    override suspend fun delete(id: Long) {
        dataSource.delete(id)
    }
}
