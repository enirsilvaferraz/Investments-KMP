package com.eferraz.usecases.services

import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import com.eferraz.usecases.repositories.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
public class ExportToCsvUseCaseTest {

    /**
     * Loads history for the date provider month, keeps only fixed income rows, then exports those.
     */
    @Test
    public fun `GIVEN mixed history rows WHEN export with Unit THEN exportToCSV receives only fixed income`() = runTest {

        // GIVEN
        val period = YearMonth(2024, Month.JANUARY)
        val dateProvider = mockk<DateProvider> {
            every { getCurrentYearMonth() } returns period
        }
        val repository = mockk<HoldingHistoryRepository>(relaxed = true)
        val fixedHolding = createAssetHolding(asset = createFixedIncomeAsset())
        val variableHolding = createAssetHolding(asset = createVariableIncomeAsset())
        val fixedEntry = createHoldingHistoryEntry(holding = fixedHolding, referenceDate = period)
        val variableEntry = createHoldingHistoryEntry(holding = variableHolding, referenceDate = period)
        coEvery { repository.getByReferenceDate(period) } returns listOf(fixedEntry, variableEntry)
        coJustRun { repository.exportToCSV(any()) }
        val useCase = ExportToCsvUseCase(
            repository = repository,
            dateProvider = dateProvider,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        useCase(Unit).getOrThrow()

        // THEN
        coVerify(exactly = 1) { repository.getByReferenceDate(period) }
        coVerify(exactly = 1) { repository.exportToCSV(listOf(fixedEntry)) }
    }
}
