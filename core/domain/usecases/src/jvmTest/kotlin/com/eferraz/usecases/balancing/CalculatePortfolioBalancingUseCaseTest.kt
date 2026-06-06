package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.holdings.Owner
import com.eferraz.usecases.cruds.GetHoldingHistoriesUseCase
import com.eferraz.usecases.repositories.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
public class CalculatePortfolioBalancingUseCaseTest {

    private val period = YearMonth(2024, Month.MARCH)
    private val issuer = Issuer(id = 1, name = "Issuer")

    /**
     * Use case orchestrates DateProvider and GetHoldingHistoriesUseCase without CreateHistoryUseCase.
     */
    @Test
    public fun `GIVEN mocked dependencies WHEN invoke with Unit THEN returns report for current period`() = runTest {

        // GIVEN
        val dateProvider = mockk<DateProvider> {
            every { getCurrentYearMonth() } returns period
        }
        val rfAsset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entry = HoldingHistoryEntry(
            holding = AssetHolding(
                id = 1,
                asset = rfAsset,
                owner = Owner(1, "Owner"),
                brokerage = Brokerage(1, "Broker"),
            ),
            referenceDate = period,
            endOfMonthValue = 10_000.0,
            endOfMonthQuantity = 1.0,
        )
        val repository = mockk<HoldingHistoryRepository>()
        coEvery { repository.getByReferenceDate(period) } returns listOf(entry)
        val getHoldingHistoriesUseCase = GetHoldingHistoriesUseCase(
            historyRepository = repository,
            context = Dispatchers.Unconfined,
        )
        val useCase = CalculatePortfolioBalancingUseCase(
            dateProvider = dateProvider,
            getHoldingHistoriesUseCase = getHoldingHistoriesUseCase,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(Unit)

        // THEN
        val report = result.getOrThrow()
        assertEquals(period, report.referenceDate)
        assertEquals(10_000.0, report.totalPortfolioValue, 0.01)
        assertEquals(7, report.lines.size)
    }

    /**
     * Repository failure propagates as Result failure (US3 AC5 / FR-014).
     */
    @Test
    public fun `GIVEN repository failure WHEN invoke with Unit THEN returns failure result`() = runTest {

        // GIVEN
        val dateProvider = mockk<DateProvider> {
            every { getCurrentYearMonth() } returns period
        }
        val repository = mockk<HoldingHistoryRepository>()
        coEvery { repository.getByReferenceDate(period) } throws IllegalStateException("History unavailable")
        val getHoldingHistoriesUseCase = GetHoldingHistoriesUseCase(
            historyRepository = repository,
            context = Dispatchers.Unconfined,
        )
        val useCase = CalculatePortfolioBalancingUseCase(
            dateProvider = dateProvider,
            getHoldingHistoriesUseCase = getHoldingHistoriesUseCase,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(Unit)

        // THEN
        assertTrue(result.isFailure)
        assertEquals("History unavailable", result.exceptionOrNull()?.message)
    }
}
