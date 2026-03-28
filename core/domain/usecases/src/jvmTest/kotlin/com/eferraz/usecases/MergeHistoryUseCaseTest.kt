package com.eferraz.usecases

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.holdings.Owner
import com.eferraz.usecases.holdings.CreateHistoryUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MergeHistoryUseCaseTest {

    private lateinit var mockAssetHoldingRepository: AssetHoldingRepository
    private lateinit var mockHoldingHistoryRepository: HoldingHistoryRepository
    private lateinit var mockCreateHistoryUseCase: CreateHistoryUseCase
    private lateinit var mockAssetTransactionRepository: AssetTransactionRepository
    private lateinit var mergeHistoryUseCase: MergeHistoryUseCase

    @BeforeTest
    fun setup() {
        mockAssetHoldingRepository = mockk<AssetHoldingRepository>(relaxed = true)
        mockHoldingHistoryRepository = mockk<HoldingHistoryRepository>(relaxed = true)
        mockCreateHistoryUseCase = mockk<CreateHistoryUseCase>(relaxed = true)
        mockAssetTransactionRepository = mockk<AssetTransactionRepository>(relaxed = true)

        mergeHistoryUseCase = MergeHistoryUseCase(
            holdingHistoryRepository = mockHoldingHistoryRepository,
            assetHoldingRepository = mockAssetHoldingRepository,
            createHistoryUseCase = mockCreateHistoryUseCase,
            assetTransactionRepository = mockAssetTransactionRepository,
            context = Dispatchers.Unconfined,
        )
    }

    // region Happy path: full history for all holdings

    @Test
    fun `GIVEN holdings with complete history THEN return results with current and previous entries`() = runTest {

        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2, asset = fixedIncomeAsset())

        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 150.0)
        val previousEntry1 = holdingHistoryEntry(id = 2, holding = holding1, referenceDate = previousDate, endOfMonthValue = 100.0)

        val currentEntry2 = holdingHistoryEntry(id = 3, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val previousEntry2 = holdingHistoryEntry(id = 4, holding = holding2, referenceDate = previousDate, endOfMonthValue = 180.0)

        val holdings = listOf(holding1, holding2)
        val category = InvestmentCategory.VARIABLE_INCOME

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(2, result.size)

        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(holding1, result1.holding)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(holding2, result2.holding)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(previousEntry2, result2.previousEntry)

        // Verify calls
        coVerify(exactly = 1) { mockAssetHoldingRepository.getAll() }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(previousDate) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any()) }

        // Order preserved
        assertEquals(holding1.id, result[0].holding.id)
        assertEquals(holding2.id, result[1].holding.id)
    }

    // endregion

    // region Holdings without previous month history

    @Test
    fun `GIVEN holdings without previous history THEN return results with default previousEntry`() = runTest {

        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2)

        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = holdingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)

        val holdings = listOf(holding1, holding2)
        val defaultPreviousEntry1 = holdingHistoryEntry(holding = holding1, referenceDate = previousDate)
        val defaultPreviousEntry2 = holdingHistoryEntry(holding = holding2, referenceDate = previousDate)
        val category = InvestmentCategory.VARIABLE_INCOME

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) } returns Result.success(defaultPreviousEntry1)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) } returns Result.success(defaultPreviousEntry2)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(2, result.size)

        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousDate, result1.previousEntry.referenceDate)
        assertEquals(holding1, result1.previousEntry.holding)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(previousDate, result2.previousEntry.referenceDate)
        assertEquals(holding2, result2.previousEntry.holding)

        // Verify createHistoryUseCase built previous entries
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) }
    }

    // endregion

    // region Holdings without current month history

    @Test
    fun `GIVEN holdings without current history THEN return results with default currentEntry`() = runTest {

        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2)

        val previousEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = holdingHistoryEntry(id = 2, holding = holding2, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2)
        val defaultCurrentEntry1 = holdingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val defaultCurrentEntry2 = holdingHistoryEntry(holding = holding2, referenceDate = referenceDate)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) } returns Result.success(defaultCurrentEntry1)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding2)) } returns Result.success(defaultCurrentEntry2)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(2, result.size)

        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(referenceDate, result1.currentEntry.referenceDate)
        assertEquals(holding1, result1.currentEntry.holding)
        assertEquals(previousEntry1, result1.previousEntry)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(referenceDate, result2.currentEntry.referenceDate)
        assertEquals(holding2, result2.currentEntry.holding)
        assertEquals(previousEntry2, result2.previousEntry)

        // Verify createHistoryUseCase built current entries
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding2)) }
    }

    // endregion

    // region Holdings without any history rows

    @Test
    fun `GIVEN holdings without any history THEN return results with default entries`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2)
        val holding3 = assetHolding(id = 3, asset = investmentFundAsset())

        val holdings = listOf(holding1, holding2, holding3)
        val defaultCurrentEntry1 = holdingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val defaultCurrentEntry2 = holdingHistoryEntry(holding = holding2, referenceDate = referenceDate)
        val defaultCurrentEntry3 = holdingHistoryEntry(holding = holding3, referenceDate = referenceDate)
        val defaultPreviousEntry1 = holdingHistoryEntry(holding = holding1, referenceDate = previousDate)
        val defaultPreviousEntry2 = holdingHistoryEntry(holding = holding2, referenceDate = previousDate)
        val defaultPreviousEntry3 = holdingHistoryEntry(holding = holding3, referenceDate = previousDate)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) } returns Result.success(defaultCurrentEntry1)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding2)) } returns Result.success(defaultCurrentEntry2)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) } returns Result.success(defaultCurrentEntry3)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) } returns Result.success(defaultPreviousEntry1)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) } returns Result.success(defaultPreviousEntry2)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding3)) } returns Result.success(defaultPreviousEntry3)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(3, result.size)

        result.forEach { resultItem ->
            assertTrue(resultItem.holding.id in listOf(1L, 2L, 3L))
            assertEquals(referenceDate, resultItem.currentEntry.referenceDate)
            assertEquals(resultItem.holding, resultItem.currentEntry.holding)
            assertEquals(previousDate, resultItem.previousEntry.referenceDate)
            assertEquals(resultItem.holding, resultItem.previousEntry.holding)
        }

        // Verify createHistoryUseCase invoked twice per holding
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding2)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding3)) }
    }

    // endregion

    // region Empty holdings list

    @Test
    fun `GIVEN empty holdings list THEN return empty list`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holdings = emptyList<AssetHolding>()

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertTrue(result.isEmpty())

        // Repositories still queried when holdings list is empty
        coVerify(exactly = 1) { mockAssetHoldingRepository.getAll() }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(previousDate) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any()) }
    }

    // endregion

    // region Holdings order preserved

    @Test
    fun `GIVEN multiple holdings THEN return results in same order as repository`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2)
        val holding3 = assetHolding(id = 3)
        val holding4 = assetHolding(id = 4)

        val holdings = listOf(holding1, holding2, holding3, holding4)
        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = holdingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)
        val currentEntry3 = holdingHistoryEntry(id = 3, holding = holding3, referenceDate = referenceDate)
        val currentEntry4 = holdingHistoryEntry(id = 4, holding = holding4, referenceDate = referenceDate)

        val previousEntry1 = holdingHistoryEntry(id = 5, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = holdingHistoryEntry(id = 6, holding = holding2, referenceDate = previousDate)
        val previousEntry3 = holdingHistoryEntry(id = 7, holding = holding3, referenceDate = previousDate)
        val previousEntry4 = holdingHistoryEntry(id = 8, holding = holding4, referenceDate = previousDate)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2, currentEntry3, currentEntry4)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2, previousEntry3, previousEntry4)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(4, result.size)
        assertEquals(holding1.id, result[0].holding.id)
        assertEquals(holding2.id, result[1].holding.id)
        assertEquals(holding3.id, result[2].holding.id)
        assertEquals(holding4.id, result[3].holding.id)

        // Result order matches holdings order
        result.forEachIndexed { index, resultItem ->
            assertEquals(holdings[index].id, resultItem.holding.id)
        }
    }

    // endregion

    // region Partial history per holding

    @Test
    fun `GIVEN holdings with partial history THEN return correct results and call createHistoryUseCase only when needed`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        // Holding 1: full history
        val holding1 = assetHolding(id = 1)
        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 150.0)
        val previousEntry1 = holdingHistoryEntry(id = 2, holding = holding1, referenceDate = previousDate, endOfMonthValue = 100.0)

        // Holding 2: current month only
        val holding2 = assetHolding(id = 2)
        val currentEntry2 = holdingHistoryEntry(id = 3, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val defaultPreviousEntry2 = holdingHistoryEntry(holding = holding2, referenceDate = previousDate)

        // Holding 3: previous month only
        val holding3 = assetHolding(id = 3)
        val previousEntry3 = holdingHistoryEntry(id = 4, holding = holding3, referenceDate = previousDate, endOfMonthValue = 300.0)
        val defaultCurrentEntry3 = holdingHistoryEntry(holding = holding3, referenceDate = referenceDate)

        // Holding 4: no history
        val holding4 = assetHolding(id = 4)
        val defaultCurrentEntry4 = holdingHistoryEntry(holding = holding4, referenceDate = referenceDate)
        val defaultPreviousEntry4 = holdingHistoryEntry(holding = holding4, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2, holding3, holding4)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry3)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) } returns Result.success(defaultPreviousEntry2)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) } returns Result.success(defaultCurrentEntry3)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding4)) } returns Result.success(defaultCurrentEntry4)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding4)) } returns Result.success(defaultPreviousEntry4)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(4, result.size)

        // Holding 1: full history
        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        // Holding 2: current only
        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(defaultPreviousEntry2, result2.previousEntry)

        // Holding 3: previous only
        val result3 = result.find { it.holding.id == 3L }
        assertNotNull(result3)
        assertEquals(defaultCurrentEntry3, result3.currentEntry)
        assertEquals(previousEntry3, result3.previousEntry)

        // Holding 4: no history rows
        val result4 = result.find { it.holding.id == 4L }
        assertNotNull(result4)
        assertEquals(defaultCurrentEntry4, result4.currentEntry)
        assertEquals(defaultPreviousEntry4, result4.previousEntry)

        // Verify createHistoryUseCase only when needed
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding4)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding4)) }
    }

    // endregion

    // region Multiple holdings with different asset types

    @Test
    fun `GIVEN holdings with different asset types THEN return correct results for each type`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2, asset = fixedIncomeAsset())
        val holding3 = assetHolding(id = 3, asset = investmentFundAsset())

        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 100.0)
        val currentEntry2 = holdingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val currentEntry3 = holdingHistoryEntry(id = 3, holding = holding3, referenceDate = referenceDate, endOfMonthValue = 300.0)

        val previousEntry1 = holdingHistoryEntry(id = 4, holding = holding1, referenceDate = previousDate, endOfMonthValue = 90.0)
        val previousEntry2 = holdingHistoryEntry(id = 5, holding = holding2, referenceDate = previousDate, endOfMonthValue = 180.0)
        val previousEntry3 = holdingHistoryEntry(id = 6, holding = holding3, referenceDate = previousDate, endOfMonthValue = 270.0)

        val holdings = listOf(holding1, holding2, holding3)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2, currentEntry3)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2, previousEntry3)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(3, result.size)

        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(previousEntry2, result2.previousEntry)

        val result3 = result.find { it.holding.id == 3L }
        assertNotNull(result3)
        assertEquals(currentEntry3, result3.currentEntry)
        assertEquals(previousEntry3, result3.previousEntry)

        // createHistoryUseCase not used when all rows exist
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any()) }
    }

    // endregion

    // region Repository mapping and missing holding rows

    @Test
    fun `GIVEN repository returns entries not matching holdings THEN map correctly and create missing entries`() = runTest {
        // GIVEN
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = assetHolding(id = 1)
        val holding2 = assetHolding(id = 2)
        val holding3 = assetHolding(id = 3)

        // Repository returns history for holding1 and holding2 only
        val currentEntry1 = holdingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = holdingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)
        val previousEntry1 = holdingHistoryEntry(id = 3, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = holdingHistoryEntry(id = 4, holding = holding2, referenceDate = previousDate)

        val defaultCurrentEntry3 = holdingHistoryEntry(holding = holding3, referenceDate = referenceDate)
        val defaultPreviousEntry3 = holdingHistoryEntry(holding = holding3, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2, holding3)

        val category = InvestmentCategory.VARIABLE_INCOME
        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) } returns Result.success(defaultCurrentEntry3)
        coEvery { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding3)) } returns Result.success(defaultPreviousEntry3)

        // WHEN
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate, category)).getOrThrow()

        // THEN
        assertEquals(3, result.size)

        // Holdings with repository rows mapped correctly
        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(previousEntry2, result2.previousEntry)

        // Holding without history gets default entries
        val result3 = result.find { it.holding.id == 3L }
        assertNotNull(result3)
        assertEquals(defaultCurrentEntry3, result3.currentEntry)
        assertEquals(defaultPreviousEntry3, result3.previousEntry)

        // createHistoryUseCase only for holding3
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding1)) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding1)) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding2)) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding2)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding3)) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(CreateHistoryUseCase.Param(previousDate, holding3)) }
    }

    // endregion

    private companion object {

        private val defaultIssuer: Issuer = Issuer(1L, "Issuer Name", false)
        private val defaultOwner: Owner = Owner(1L, "Owner Name")
        private val defaultBrokerage: Brokerage = Brokerage(1L, "Brokerage Name")

        private fun variableIncomeAsset(id: Long = 1L, ticker: String = "TICKER"): VariableIncomeAsset =
            VariableIncomeAsset(
                id = id,
                name = "Variable Asset",
                issuer = defaultIssuer,
                type = VariableIncomeAssetType.NATIONAL_STOCK,
                ticker = ticker,
            )

        private fun fixedIncomeAsset(id: Long = 2L): FixedIncomeAsset =
            FixedIncomeAsset(
                id = id,
                issuer = defaultIssuer,
                type = FixedIncomeAssetType.PRE_FIXED,
                subType = FixedIncomeSubType.CDB,
                expirationDate = LocalDate(2025, Month.JANUARY, 1),
                contractedYield = 10.0,
                liquidity = Liquidity.D_PLUS_DAYS,
            )

        private fun investmentFundAsset(id: Long = 3L): InvestmentFundAsset =
            InvestmentFundAsset(
                id = id,
                name = "Investment Fund",
                issuer = defaultIssuer,
                type = InvestmentFundAssetType.MULTIMARKET_FUND,
                liquidity = Liquidity.D_PLUS_DAYS,
                liquidityDays = 1,
                expirationDate = null,
            )

        private fun assetHolding(id: Long = 1L, asset: Asset = variableIncomeAsset()): AssetHolding =
            AssetHolding(id, asset, defaultOwner, defaultBrokerage)

        private fun holdingHistoryEntry(
            id: Long? = null,
            holding: AssetHolding = assetHolding(),
            referenceDate: YearMonth = YearMonth(2024, Month.JANUARY),
            endOfMonthValue: Double = 100.0,
            endOfMonthQuantity: Double = 10.0,
            endOfMonthAverageCost: Double = 10.0,
            totalInvested: Double = 100.0,
        ): HoldingHistoryEntry =
            HoldingHistoryEntry(
                id,
                holding,
                referenceDate,
                endOfMonthValue,
                endOfMonthQuantity,
                endOfMonthAverageCost,
                totalInvested,
            )
    }
}

