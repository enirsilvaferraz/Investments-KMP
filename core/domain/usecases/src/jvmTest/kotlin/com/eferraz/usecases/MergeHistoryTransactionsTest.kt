package com.eferraz.usecases

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import io.mockk.coEvery
import io.mockk.mockk
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
class MergeHistoryTransactionsTest {

    /**
     * MergeHistory filters embedded holding.transactions for the reference month (SC-001 contract).
     */
    @Test
    fun `GIVEN holding with embedded transactions WHEN merge THEN uses month filter on holding list`() = runTest {

        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val inMonth = AssetTransaction(
            id = 1L,
            date = LocalDate(2024, 4, 15),
            type = TransactionType.PURCHASE,
            quantity = 1.0,
            unitPrice = 500.0,
        )
        val outOfMonth = AssetTransaction(
            id = 2L,
            date = LocalDate(2024, 3, 1),
            type = TransactionType.PURCHASE,
            quantity = 1.0,
            unitPrice = 999.0,
        )
        val holding = createAssetHolding(
            id = 1L,
            asset = createFixedIncomeAsset(),
            transactions = listOf(inMonth, outOfMonth),
        )
        val currentEntry = createHoldingHistoryEntry(
            holding = holding,
            referenceDate = referenceDate,
            endOfMonthValue = 100.0,
        )
        val previousEntry = createHoldingHistoryEntry(
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 90.0,
        )

        val holdingRepository = mockk<AssetHoldingRepository>()
        val historyRepository = mockk<HoldingHistoryRepository>()

        coEvery { holdingRepository.getAll() } returns listOf(holding)
        coEvery { historyRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry)
        coEvery { historyRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry)

        val useCase = MergeHistoryUseCase(
            holdingHistoryRepository = historyRepository,
            assetHoldingRepository = holdingRepository,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // THEN
        assertEquals(1, result.size)
        val monthTransactions = holding.transactions.filter { tx ->
            tx.date.year == referenceDate.year && tx.date.month == referenceDate.month
        }
        val balance = TransactionBalance.calculate(monthTransactions)
        assertEquals(500.0, balance.contributions, 0.01)
        assertEquals(1, monthTransactions.size)
    }
}
