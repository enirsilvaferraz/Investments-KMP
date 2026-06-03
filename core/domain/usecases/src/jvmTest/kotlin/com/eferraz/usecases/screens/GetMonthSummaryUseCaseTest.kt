package com.eferraz.usecases.screens

import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetMonthSummaryUseCaseTest {

    private val useCase = GetMonthSummaryUseCase(context = Dispatchers.Unconfined)

    private val referenceDate = YearMonth(2026, Month.MAY)

    /**
     * Two holdings with previous/current values and one contribution aggregate into month summary.
     */
    @Test
    fun `GIVEN filtered current and previous entries WHEN execute THEN returns consolidated month summary`() = runTest {

        // GIVEN
        val asset = createFixedIncomeAsset()
        val holdingOne = createAssetHolding(id = 1L, asset = asset)
        val holdingTwo = createAssetHolding(id = 2L, asset = asset)

        val contribution = FixedIncomeTransaction(
            id = 1L,
            date = LocalDate(2026, Month.MAY, 10),
            type = TransactionType.PURCHASE,
            totalValue = 500.0,
        )
        val holdingOneWithTransactions = holdingOne.copy(transactions = listOf(contribution))

        val currentEntries = listOf(
            createHoldingHistoryEntry(
                holding = holdingOneWithTransactions,
                referenceDate = referenceDate,
                endOfMonthValue = 100.0,
                endOfMonthQuantity = 1.0,
            ),
            createHoldingHistoryEntry(
                holding = holdingTwo,
                referenceDate = referenceDate,
                endOfMonthValue = 50.0,
                endOfMonthQuantity = 2.0,
            ),
        )
        val previousEntries = listOf(
            createHoldingHistoryEntry(
                holding = holdingOne,
                referenceDate = referenceDate.minusMonth(),
                endOfMonthValue = 80.0,
                endOfMonthQuantity = 1.0,
            ),
            createHoldingHistoryEntry(
                holding = holdingTwo,
                referenceDate = referenceDate.minusMonth(),
                endOfMonthValue = 40.0,
                endOfMonthQuantity = 2.0,
            ),
        )

        // WHEN
        val summary = useCase(
            GetMonthSummaryUseCase.Param(
                referenceDate = referenceDate,
                current = currentEntries,
                previous = previousEntries,
            ),
        ).getOrThrow()

        // THEN
        assertEquals(160.0, summary.previousValue, 0.01)
        assertEquals(200.0, summary.actualValue, 0.01)
        assertEquals(500.0, summary.contributions, 0.01)
        assertEquals(0.0, summary.withdrawals, 0.01)
        assertEquals(40.0, summary.growth, 0.01)
        assertEquals(25.0, summary.growthPercent, 0.01)
        assertEquals(-460.0, summary.earnings, 0.01)
        assertEquals(-287.5, summary.earningsPercent, 0.01)
    }

    /**
     * Empty entry list yields zeroed summary.
     */
    @Test
    fun `GIVEN empty entries WHEN execute THEN returns zero month summary`() = runTest {

        // GIVEN
        val param = GetMonthSummaryUseCase.Param(
            referenceDate = referenceDate,
            current = emptyList(),
            previous = emptyList(),
        )

        // WHEN
        val summary = useCase(param).getOrThrow()

        // THEN
        assertEquals(0.0, summary.previousValue, 0.01)
        assertEquals(0.0, summary.actualValue, 0.01)
        assertEquals(0.0, summary.contributions, 0.01)
        assertEquals(0.0, summary.withdrawals, 0.01)
        assertEquals(0.0, summary.growth, 0.01)
        assertEquals(0.0, summary.growthPercent, 0.01)
        assertEquals(0.0, summary.earnings, 0.01)
        assertEquals(0.0, summary.earningsPercent, 0.01)
    }
}
