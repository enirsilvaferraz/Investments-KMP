package com.eferraz.repositories

import com.eferraz.database.datasources.HoldingHistoryDataSource
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [HoldingHistoryRepository::class])
internal class HoldingHistoryRepositoryImpl(
    private val dataSource: HoldingHistoryDataSource,
) : HoldingHistoryRepository {

    override fun getByReferenceDateAndPrevious(referenceDate: YearMonth) =
        dataSource.getByReferenceDateAndPrevious(referenceDate)

    override suspend fun update(entry: HoldingHistoryEntry) {
        dataSource.update(entry)
    }

    override suspend fun insert(entry: HoldingHistoryEntry) =
        dataSource.insert(entry)
}

