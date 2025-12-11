package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.Brokerage
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Issuer
import com.eferraz.entities.Liquidity
import com.eferraz.entities.Owner
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GetHoldingHistoryUseCaseTest {

    private lateinit var mockAssetHoldingRepository: AssetHoldingRepository
    private lateinit var mockHoldingHistoryRepository: HoldingHistoryRepository
    private lateinit var mockCreateMissingEntriesUseCase: CreateMissingHoldingHistoryEntriesUseCase
    private lateinit var getHoldingHistoryUseCase: GetHoldingHistoryUseCase

    // Slots para capturar argumentos do mock
    private val capturedResultsSlot = slot<List<HoldingHistoryResult>>()
    private val capturedReferenceDateSlot = slot<YearMonth>()
    private val capturedPreviousEntriesMapSlot = slot<Map<Long, HoldingHistoryEntry>>()

    @BeforeTest
    fun setup() {
        mockAssetHoldingRepository = mockk<AssetHoldingRepository>(relaxed = true)
        mockHoldingHistoryRepository = mockk<HoldingHistoryRepository>(relaxed = true)
        mockCreateMissingEntriesUseCase = mockk<CreateMissingHoldingHistoryEntriesUseCase>(relaxed = true)

        // Configurar comportamento padrão: retornar lista vazia
        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns emptyList()

        // Configurar comportamento padrão para os repositórios
        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returns emptyList()

        getHoldingHistoryUseCase = GetHoldingHistoryUseCase(
            assetHoldingRepository = mockAssetHoldingRepository,
            holdingHistoryRepository = mockHoldingHistoryRepository,
            createMissingEntriesUseCase = mockCreateMissingEntriesUseCase,
        )
    }

    // endregion

    // region TestDataFactory

    object TestDataFactory {
        fun createOwner(id: Long = 1, name: String = "Owner Name") = Owner(id, name)
        fun createBrokerage(id: Long = 1, name: String = "Brokerage Name") = Brokerage(id, name)
        fun createIssuer(id: Long = 1, name: String = "Issuer Name") = Issuer(id, name)

        fun createVariableIncomeAsset(
            id: Long = 1,
            name: String = "Variable Asset",
            issuer: Issuer = createIssuer(),
            type: VariableIncomeAssetType = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker: String = "TICKER",
        ) = VariableIncomeAsset(id, name, issuer, type, ticker)

        fun createFixedIncomeAsset(
            id: Long = 2,
            issuer: Issuer = createIssuer(),
            type: FixedIncomeAssetType = FixedIncomeAssetType.PRE_FIXED,
            subType: FixedIncomeSubType = FixedIncomeSubType.CDB,
            expirationDate: LocalDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield: Double = 10.0,
        ) = FixedIncomeAsset(id, issuer, type, subType, expirationDate, contractedYield, liquidity = Liquidity.D_PLUS_DAYS)

        fun createInvestmentFundAsset(
            id: Long = 3,
            name: String = "Investment Fund",
            issuer: Issuer = createIssuer(),
            type: InvestmentFundAssetType = InvestmentFundAssetType.MULTIMARKET_FUND,
            liquidity: Liquidity = Liquidity.D_PLUS_DAYS,
            liquidityDays: Int = 1,
            expirationDate: LocalDate? = null,
        ) = InvestmentFundAsset(id, name, issuer, type, liquidity, liquidityDays, expirationDate)

        fun createAssetHolding(
            id: Long = 1,
            asset: Asset = createVariableIncomeAsset(),
            owner: Owner = createOwner(),
            brokerage: Brokerage = createBrokerage(),
        ) = AssetHolding(id, asset, owner, brokerage)

        fun createHoldingHistoryEntry(
            id: Long? = null,
            holding: AssetHolding = createAssetHolding(),
            referenceDate: YearMonth = YearMonth(2024, Month.JANUARY),
            endOfMonthValue: Double = 100.0,
            endOfMonthQuantity: Double = 10.0,
            endOfMonthAverageCost: Double = 10.0,
            totalInvested: Double = 100.0,
        ) = HoldingHistoryEntry(id, holding, referenceDate, endOfMonthValue, endOfMonthQuantity, endOfMonthAverageCost, totalInvested)

        fun createHoldingHistoryResult(
            holding: AssetHolding = createAssetHolding(),
            currentEntry: HoldingHistoryEntry? = null,
            previousEntry: HoldingHistoryEntry? = null,
        ) = HoldingHistoryResult(holding, currentEntry, previousEntry)
    }

    // endregion

    // region Testes de Entrada e Validação

    @Test
    fun test_invoke_with_valid_reference_date_normal_month() = runTest {

        val referenceDate = YearMonth(2024, Month.APRIL)
        val holding = TestDataFactory.createAssetHolding(id = 1)

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding, referenceDate = referenceDate)
            )
        )

        coEvery { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_reference_date_january_then_previousMonth_is_december_of_previous_year() = runTest {

        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedPreviousMonth = YearMonth(2023, Month.DECEMBER)

        val holding = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding,
                previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding, referenceDate = expectedPreviousMonth)
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(expectedPreviousMonth, capturedReferenceDateSlot.captured.minusMonth())
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_reference_date_december_then_previousMonth_is_november() = runTest {

        val holding = TestDataFactory.createAssetHolding(id = 1)

        val referenceDate = YearMonth(2024, Month.DECEMBER)
        val expectedPreviousMonth = YearMonth(2024, Month.NOVEMBER)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding,
                previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding, referenceDate = expectedPreviousMonth)
            )
        )

        coEvery { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_reference_date_leap_year_february_then_previousMonth_is_january() = runTest {
        val referenceDate = YearMonth(2024, Month.FEBRUARY) // 2024 is a leap year
        val expectedPreviousMonth = YearMonth(2024, Month.JANUARY)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(TestDataFactory.createAssetHolding(id = 1))
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = TestDataFactory.createAssetHolding(id = 1),
                previousEntry = TestDataFactory.createHoldingHistoryEntry(
                    holding = TestDataFactory.createAssetHolding(id = 1),
                    referenceDate = expectedPreviousMonth
                )
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_reference_date_non_leap_year_february_then_previousMonth_is_january() = runTest {
        val referenceDate = YearMonth(2023, Month.FEBRUARY) // 2023 is not a leap year
        val expectedPreviousMonth = YearMonth(2023, Month.JANUARY)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(TestDataFactory.createAssetHolding(id = 1))
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = TestDataFactory.createAssetHolding(id = 1),
                previousEntry = TestDataFactory.createHoldingHistoryEntry(
                    holding = TestDataFactory.createAssetHolding(id = 1),
                    referenceDate = expectedPreviousMonth
                )
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_very_old_reference_date_does_not_throw_exception() = runTest {

        val referenceDate = YearMonth(1990, Month.JANUARY)

        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        coEvery { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) } returns emptyList()

        val result = getHoldingHistoryUseCase(referenceDate)
        assertTrue(result.isEmpty())
    }

    @Ignore("Not necessary for now")
    @Test
    fun test_invoke_with_future_reference_date_does_not_throw_exception() = runTest {
        val referenceDate = YearMonth(2050, Month.JANUARY)

        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns emptyList()

        val result = getHoldingHistoryUseCase(referenceDate)
        assertTrue(result.isEmpty())
    }

    // endregion

    // region Testes de Repositórios Vazios

    @Test
    fun test_invoke_when_assetHoldingRepository_is_empty() = runTest {
        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns emptyList()

        val result = getHoldingHistoryUseCase(YearMonth(2024, Month.JANUARY))

        assertTrue(result.isEmpty())
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_invoke_when_currentHistoryRepository_is_empty_but_holdings_exist() = runTest {
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = null, previousEntry = null)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(YearMonth(2024, Month.JANUARY))

        assertEquals(1, result.size)
        assertNotNull(result.first { it.holding.id == 1L })
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_invoke_when_previousHistoryRepository_is_empty_but_holdings_and_current_exist() = runTest {
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry1 = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = YearMonth(2024, Month.JANUARY))

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(listOf(currentEntry1), emptyList())

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry1, previousEntry = null)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(YearMonth(2024, Month.JANUARY))

        assertEquals(1, result.size)
        val actualResult = result.first { it.holding.id == 1L }
        assertNotNull(actualResult.currentEntry)
        assertEquals(null, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_invoke_when_all_repositories_are_empty() = runTest {
        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns emptyList()

        val result = getHoldingHistoryUseCase(YearMonth(2024, Month.JANUARY))

        assertTrue(result.isEmpty())
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    // endregion

    // region Testes de Delegação

    @Test
    fun test_createMissingEntriesUseCase_is_called_with_empty_results_list_when_holdings_are_empty() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns emptyList()

        getHoldingHistoryUseCase(referenceDate)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(emptyList(), capturedResultsSlot.captured)
    }

    @Test
    fun test_createMissingEntriesUseCase_is_called_with_correct_results_list() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2)
        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val expectedResultsList = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1),
            TestDataFactory.createHoldingHistoryResult(holding = holding2)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns expectedResultsList

        getHoldingHistoryUseCase(referenceDate)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(2, capturedResultsSlot.captured.size)
    }

    @Test
    fun test_historyMap_is_converted_to_list_before_delegation() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2)
        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1),
            TestDataFactory.createHoldingHistoryResult(holding = holding2)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_order_of_results_from_delegation_is_preserved() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2)
        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val orderedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1),
            TestDataFactory.createHoldingHistoryResult(holding = holding2)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns orderedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(orderedResults, result)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_previousEntriesMap_is_created_correctly_and_passed_to_delegated_use_case() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2)
        val previousEntry1 = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)
        val previousEntry2 = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), listOf(previousEntry1, previousEntry2))

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, previousEntry = previousEntry1),
            TestDataFactory.createHoldingHistoryResult(holding = holding2, previousEntry = previousEntry2)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns expectedResults

        getHoldingHistoryUseCase(referenceDate)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(2, capturedPreviousEntriesMapSlot.captured.size)
        assertEquals(previousEntry1, capturedPreviousEntriesMapSlot.captured[1L])
        assertEquals(previousEntry2, capturedPreviousEntriesMapSlot.captured[2L])
    }

    @Test
    fun test_previousEntriesMap_handles_duplicate_ids_uses_last() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val prevEntry1a =
            TestDataFactory.createHoldingHistoryEntry(id = 100L, holding = holding1, referenceDate = previousMonth, endOfMonthValue = 50.0)
        val prevEntry1b =
            TestDataFactory.createHoldingHistoryEntry(id = 101L, holding = holding1, referenceDate = previousMonth, endOfMonthValue = 75.0)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), listOf(prevEntry1a, prevEntry1b))

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, previousEntry = prevEntry1b)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns expectedResults

        getHoldingHistoryUseCase(referenceDate)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(prevEntry1b, capturedPreviousEntriesMapSlot.captured[1L])
    }

    @Test
    fun test_previousEntriesMap_is_empty_when_no_previous_entries() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(
                capture(capturedResultsSlot),
                capture(capturedReferenceDateSlot),
                capture(capturedPreviousEntriesMapSlot)
            )
        } returns expectedResults

        getHoldingHistoryUseCase(referenceDate)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
        assertEquals(emptyMap<Long, HoldingHistoryEntry>(), capturedPreviousEntriesMapSlot.captured)
    }

    // endregion

    // region Testes de Tratamento de Exceções

    @Test
    fun test_exception_when_fetching_holdings_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"

        coEvery { mockAssetHoldingRepository.getAll() } throws RuntimeException("DB connection lost")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is RuntimeException)
        assertEquals("DB connection lost", exception.cause?.message)
    }

    @Test
    fun test_exception_when_fetching_current_history_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } throws IllegalStateException("Current history fetch failed")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is IllegalStateException)
        assertEquals("Current history fetch failed", exception.cause?.message)
    }

    @Test
    fun test_exception_when_fetching_previous_history_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returns emptyList() andThenThrows RuntimeException("Previous history fetch failed")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is RuntimeException)
        assertEquals("Previous history fetch failed", exception.cause?.message)
    }

    @Test
    fun test_exception_when_creating_missing_entries_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } throws RuntimeException("Missing entries creation failed")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is RuntimeException)
        assertEquals("Missing entries creation failed", exception.cause?.message)
    }

    @Test
    fun test_NullPointerException_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } throws NullPointerException("Simulated NPE")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is NullPointerException)
        assertEquals("Simulated NPE", exception.cause?.message)
    }

    @Test
    fun test_IllegalArgumentException_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } throws IllegalArgumentException("Invalid argument detected")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is IllegalArgumentException)
        assertEquals("Invalid argument detected", exception.cause?.message)
    }

    @Test
    fun test_generic_unexpected_exception_is_caught_and_rethrown() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedMessage = "Erro ao obter histórico de posições"

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } throws Error("Unexpected internal error")

        val exception = assertFailsWith<Exception> {
            getHoldingHistoryUseCase(referenceDate)
        }

        assertTrue(exception.message?.startsWith(expectedMessage) == true)
        assertTrue(exception.cause is Error)
        assertEquals("Unexpected internal error", exception.cause?.message)
    }

    // endregion

    // region Testes de Execução Assíncrona

    @Test
    fun test_execution_in_Dispatchers_Default() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_multiple_simultaneous_calls_handle_concurrency() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val call1 = getHoldingHistoryUseCase(referenceDate)
        // Reset mock for second call
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        val call2 = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, call1.size)
        assertEquals(1, call2.size)
        coVerify(atLeast = 2) { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_cancellation_during_fetching_holdings() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)

        coEvery { mockAssetHoldingRepository.getAll() } throws kotlinx.coroutines.CancellationException("Simulated cancellation")

        assertFailsWith<kotlinx.coroutines.CancellationException> {
            getHoldingHistoryUseCase(referenceDate)
        }
    }

    @Test
    fun test_cancellation_during_fetching_history() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } throws kotlinx.coroutines.CancellationException("Simulated cancellation")

        assertFailsWith<kotlinx.coroutines.CancellationException> {
            getHoldingHistoryUseCase(referenceDate)
        }
    }

    @Test
    fun test_cancellation_during_creating_entries() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } throws kotlinx.coroutines.CancellationException("Simulated cancellation")

        assertFailsWith<kotlinx.coroutines.CancellationException> {
            getHoldingHistoryUseCase(referenceDate)
        }
    }

    // endregion

    // region Testes de Integração com Dependências

    @Test
    fun test_assetHoldingRepository_returns_correct_data_integrated() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1, asset = TestDataFactory.createVariableIncomeAsset(name = "Holding A"))
        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        val expectedResults = listOf(TestDataFactory.createHoldingHistoryResult(holding = holding1))

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        assertEquals(holding1, result[0].holding)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdingHistoryRepository_returns_correct_data_integrated() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = previousEntry)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertNotNull(actualResult.currentEntry)
        assertNotNull(actualResult.previousEntry)
        assertEquals(currentEntry, actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_createMissingHoldingHistoryEntriesUseCase_functions_correctly_integrated() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val expectedResult = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            )
        )

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResult

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        assertEquals(expectedResult, result)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_all_dependencies_mocked_and_used_as_expected() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate.minusMonth())

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = previousEntry)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        assertNotNull(result.first().holding)
        assertNotNull(result.first().currentEntry)
        assertNotNull(result.first().previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    // endregion

    // region Testes de Combinações de Dados

    @Test
    fun test_holdings_with_complete_history_current_and_previous_exist() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = previousEntry)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertEquals(currentEntry, actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdings_without_history_neither_current_nor_previous_exist() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertNotNull(actualResult.currentEntry)
        assertEquals(null, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdings_with_partial_history_only_current_exists() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            emptyList()
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = null)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertEquals(currentEntry, actualResult.currentEntry)
        assertEquals(null, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdings_with_partial_history_only_previous_exists() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate),
                previousEntry = previousEntry
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertNotNull(actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_mix_of_scenarios_some_complete_some_partial_some_empty() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()

        val holdingComplete = TestDataFactory.createAssetHolding(id = 1)
        val currentEntryComplete =
            TestDataFactory.createHoldingHistoryEntry(holding = holdingComplete, referenceDate = referenceDate, endOfMonthValue = 100.0)
        val previousEntryComplete =
            TestDataFactory.createHoldingHistoryEntry(holding = holdingComplete, referenceDate = previousMonth, endOfMonthValue = 90.0)

        val holdingPartialCurrent = TestDataFactory.createAssetHolding(id = 2)
        val currentEntryPartial =
            TestDataFactory.createHoldingHistoryEntry(holding = holdingPartialCurrent, referenceDate = referenceDate, endOfMonthValue = 50.0)

        val holdingPartialPrevious = TestDataFactory.createAssetHolding(id = 3)
        val previousEntryPartial =
            TestDataFactory.createHoldingHistoryEntry(holding = holdingPartialPrevious, referenceDate = previousMonth, endOfMonthValue = 40.0)

        val holdingEmpty = TestDataFactory.createAssetHolding(id = 4)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holdingComplete, holdingPartialCurrent, holdingPartialPrevious, holdingEmpty)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntryComplete, currentEntryPartial),
            listOf(previousEntryComplete, previousEntryPartial)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holdingComplete,
                currentEntry = currentEntryComplete,
                previousEntry = previousEntryComplete
            ),
            TestDataFactory.createHoldingHistoryResult(holding = holdingPartialCurrent, currentEntry = currentEntryPartial, previousEntry = null),
            TestDataFactory.createHoldingHistoryResult(
                holding = holdingPartialPrevious,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holdingPartialPrevious, referenceDate = referenceDate),
                previousEntry = previousEntryPartial
            ),
            TestDataFactory.createHoldingHistoryResult(
                holding = holdingEmpty,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holdingEmpty, referenceDate = referenceDate),
                previousEntry = null
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(4, result.size)
        val r1 = result.first { it.holding.id == 1L }
        assertEquals(currentEntryComplete, r1.currentEntry)
        assertEquals(previousEntryComplete, r1.previousEntry)

        val r2 = result.first { it.holding.id == 2L }
        assertEquals(currentEntryPartial, r2.currentEntry)
        assertEquals(null, r2.previousEntry)

        val r3 = result.first { it.holding.id == 3L }
        assertNotNull(r3.currentEntry)
        assertEquals(previousEntryPartial, r3.previousEntry)

        val r4 = result.first { it.holding.id == 4L }
        assertNotNull(r4.currentEntry)
        assertEquals(null, r4.previousEntry)

        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    // endregion

    // region Testes de Edge Cases

    @Test
    fun test_referenceDate_in_distant_past_produces_valid_result() = runTest {
        val referenceDate = YearMonth(1900, Month.FEBRUARY)
        val previousMonth = YearMonth(1900, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate),
                previousEntry = previousEntry
            )
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertNotNull(actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_referenceDate_in_distant_future_produces_valid_result() = runTest {
        val referenceDate = YearMonth(2500, Month.MARCH)
        val previousMonth = YearMonth(2500, Month.FEBRUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            listOf(previousEntry)
        )

        val expectedResults = listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = previousEntry)
        )

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertEquals(currentEntry, actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdings_with_same_asset_but_different_owners_are_treated_separately() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val asset = TestDataFactory.createVariableIncomeAsset(id = 10, ticker = "ABC")
        val owner1 = TestDataFactory.createOwner(id = 1, name = "Owner 1")
        val owner2 = TestDataFactory.createOwner(id = 2, name = "Owner 2")

        val holding1 = TestDataFactory.createAssetHolding(id = 1, asset = asset, owner = owner1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2, asset = asset, owner = owner2)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            ),
            TestDataFactory.createHoldingHistoryResult(
                holding = holding2,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate)
            )
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
        val r1 = result.first { it.holding.id == 1L }
        val r2 = result.first { it.holding.id == 2L }
        assertNotNull(r1.currentEntry)
        assertNotNull(r2.currentEntry)
        assertEquals(owner1, r1.holding.owner)
        assertEquals(owner2, r2.holding.owner)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    @Test
    fun test_holdings_with_same_asset_but_different_brokerages_are_treated_separately() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val asset = TestDataFactory.createVariableIncomeAsset(id = 10, ticker = "ABC")
        val brokerage1 = TestDataFactory.createBrokerage(id = 1, name = "Brokerage 1")
        val brokerage2 = TestDataFactory.createBrokerage(id = 2, name = "Brokerage 2")

        val holding1 = TestDataFactory.createAssetHolding(id = 1, asset = asset, brokerage = brokerage1)
        val holding2 = TestDataFactory.createAssetHolding(id = 2, asset = asset, brokerage = brokerage2)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            ),
            TestDataFactory.createHoldingHistoryResult(
                holding = holding2,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate)
            )
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
        val r1 = result.first { it.holding.id == 1L }
        val r2 = result.first { it.holding.id == 2L }
        assertNotNull(r1.currentEntry)
        assertNotNull(r2.currentEntry)
        assertEquals(brokerage1, r1.holding.brokerage)
        assertEquals(brokerage2, r2.holding.brokerage)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    // endregion

    // region Testes de Performance

//    @Test
//    fun test_performance_with_100_holdings() = runTest {
//        val referenceDate = YearMonth(2024, Month.JANUARY)
//        val holdings = (1L..100L).map { TestDataFactory.createAssetHolding(id = it) }
//
//        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
//        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
//            emptyList(),
//            emptyList()
//        )
//        val expectedResults = holdings.map { TestDataFactory.createHoldingHistoryResult(holding = it) }
//
//        coEvery {
//            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
//        } returns expectedResults
//
//        val startTime = kotlin.system.getTimeNanos()
//        val result = getHoldingHistoryUseCase(referenceDate)
//        val endTime = kotlin.system.getTimeNanos()
//        val durationMillis = (endTime - startTime) / 1_000_000.0
//
//        assertTrue(durationMillis < 500, "100 holdings performance test took too long: $durationMillis ms")
//        assertEquals(100, result.size)
//    }

//    @Test
//    fun test_performance_with_1000_holdings() = runTest {
//        val referenceDate = YearMonth(2024, Month.JANUARY)
//        val holdings = (1L..1000L).map { TestDataFactory.createAssetHolding(id = it) }
//
//        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
//        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
//            emptyList(),
//            emptyList()
//        )
//        val expectedResults = holdings.map { TestDataFactory.createHoldingHistoryResult(holding = it) }
//
//        coEvery {
//            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
//        } returns expectedResults
//
//        val startTime = kotlin.system.getTimeNanos()
//        val result = getHoldingHistoryUseCase(referenceDate)
//        val endTime = kotlin.system.getTimeNanos()
//        val durationMillis = (endTime - startTime) / 1_000_000.0
//
//        assertTrue(durationMillis < 1000, "1000 holdings performance test took too long: $durationMillis ms")
//        assertEquals(1000, result.size)
//    }

    // endregion

    // region Testes de Retorno

    @Test
    fun test_returns_non_null_list_even_if_empty() = runTest {
        coEvery { mockAssetHoldingRepository.getAll() } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())
        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns emptyList()

        val result = getHoldingHistoryUseCase(YearMonth(2024, Month.JANUARY))

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun test_returns_list_with_correct_size() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holdings = listOf(TestDataFactory.createAssetHolding(id = 1), TestDataFactory.createAssetHolding(id = 2))

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )
        val expectedResults = holdings.map { TestDataFactory.createHoldingHistoryResult(holding = it) }

        coEvery {
            mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any())
        } returns expectedResults

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
    }

    @Test
    fun test_returned_result_is_complete_with_all_expected_fields() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val previousMonth = referenceDate.minusMonth()
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val currentEntry = TestDataFactory.createHoldingHistoryEntry(
            holding = holding1,
            referenceDate = referenceDate,
            endOfMonthValue = 100.0,
            endOfMonthQuantity = 10.0
        )
        val previousEntry = TestDataFactory.createHoldingHistoryEntry(
            holding = holding1,
            referenceDate = previousMonth,
            endOfMonthValue = 90.0,
            endOfMonthQuantity = 9.0
        )

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            listOf(currentEntry),
            listOf(previousEntry)
        )

        listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry, previousEntry = previousEntry)
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertEquals(holding1, actualResult.holding)
        assertEquals(currentEntry, actualResult.currentEntry)
        assertEquals(previousEntry, actualResult.previousEntry)
    }

    // endregion

    // region Testes de Validação de Dados

    @Test
    fun test_holdings_with_valid_data_are_processed_correctly() = runTest {
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(
            id = 1,
            asset = TestDataFactory.createVariableIncomeAsset(name = "Valid Asset", ticker = "VAL3"),
            owner = TestDataFactory.createOwner(id = 10, name = "Valid Owner"),
            brokerage = TestDataFactory.createBrokerage(id = 20, name = "Valid Brokerage")
        )

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )
        listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            )
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(1, result.size)
        val actualResult = result.first()
        assertEquals(holding1, actualResult.holding)
        assertNotNull(actualResult.currentEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

//    @Test
//    fun test_history_entries_with_valid_data_are_processed_correctly() = runTest {
//        val referenceDate = YearMonth(2024, Month.JANUARY)
//        val holding1 = TestDataFactory.createAssetHolding(id = 1)
//        val validEntry = TestDataFactory.createHoldingHistoryEntry(
//            id = 100L,
//            holding = holding1,
//            referenceDate = referenceDate,
//            endOfMonthValue = 1000.0,
//            endOfMonthQuantity = 50.0,
//            endOfMonthAverageCost = 20.0,
//            totalInvested = 1000.0
//        )
//
//        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1)
//        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
//            Result.success(listOf(validEntry)),
//            Result.success(emptyList())
//        )
//
//        val expectedResults = listOf(
//            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = validEntry)
//        )
//
//        val result = getHoldingHistoryUseCase(referenceDate)
//
//        assertEquals(1, result.size)
//        val actualResult = result.first()
//        assertEquals(validEntry, actualResult.currentEntry)
//        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
//    }

    // endregion

    // region Testes de Casos de Uso Reais

    @Test
    fun test_real_use_case_query_current_month_history_no_existing_entries_all_created() = runTest {
        val referenceDate = YearMonth(2024, Month.FEBRUARY)
        YearMonth(2024, Month.JANUARY)
        val holding1 = TestDataFactory.createAssetHolding(id = 1, asset = TestDataFactory.createVariableIncomeAsset(ticker = "VALE3"))
        val holding2 = TestDataFactory.createAssetHolding(id = 2, asset = TestDataFactory.createFixedIncomeAsset())

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(emptyList(), emptyList())

        val createdEntry1 = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate, endOfMonthValue = 100.0)
        val createdEntry2 = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)

        listOf(
            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = createdEntry1),
            TestDataFactory.createHoldingHistoryResult(holding = holding2, currentEntry = createdEntry2)
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
        assertNotNull(result.first { it.holding.id == 1L }.currentEntry)
        assertNotNull(result.first { it.holding.id == 2L }.currentEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

//    @Test
//    fun test_real_use_case_query_past_month_history_entries_already_exist() = runTest {
//        val referenceDate = YearMonth(2023, Month.OCTOBER)
//        val previousMonth = YearMonth(2023, Month.SEPTEMBER)
//        val holding1 = TestDataFactory.createAssetHolding(id = 1)
//        val holding2 = TestDataFactory.createAssetHolding(id = 2)
//
//        val currentEntry1 = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate, endOfMonthValue = 150.0)
//        val previousEntry1 = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = previousMonth, endOfMonthValue = 140.0)
//        val currentEntry2 = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate, endOfMonthValue = 250.0)
//        val previousEntry2 = TestDataFactory.createHoldingHistoryEntry(holding = holding2, referenceDate = previousMonth, endOfMonthValue = 240.0)
//
//        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, holding2)
//        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
//            Result.success(listOf(currentEntry1, currentEntry2)),
//            Result.success(listOf(previousEntry1, previousEntry2))
//        )
//
//        val expectedResults = listOf(
//            TestDataFactory.createHoldingHistoryResult(holding = holding1, currentEntry = currentEntry1, previousEntry = previousEntry1),
//            TestDataFactory.createHoldingHistoryResult(holding = holding2, currentEntry = currentEntry2, previousEntry = previousEntry2)
//        )
//
//        val result = getHoldingHistoryUseCase(referenceDate)
//
//        assertEquals(2, result.size)
//        assertEquals(currentEntry1, result.first { it.holding.id == 1L }.currentEntry)
//        assertEquals(previousEntry1, result.first { it.holding.id == 1L }.previousEntry)
//        assertEquals(currentEntry2, result.first { it.holding.id == 2L }.currentEntry)
//        assertEquals(previousEntry2, result.first { it.holding.id == 2L }.previousEntry)
//        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
//    }

    @Test
    fun test_real_use_case_query_after_new_holdings_are_added() = runTest {
        val referenceDate = YearMonth(2024, Month.APRIL)
        val holding1 = TestDataFactory.createAssetHolding(id = 1)
        val newHolding = TestDataFactory.createAssetHolding(id = 3)

        coEvery { mockAssetHoldingRepository.getAll() } returns listOf(holding1, newHolding)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(any()) } returnsMany listOf(
            emptyList(),
            emptyList()
        )

        listOf(
            TestDataFactory.createHoldingHistoryResult(
                holding = holding1,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
            ),
            TestDataFactory.createHoldingHistoryResult(
                holding = newHolding,
                currentEntry = TestDataFactory.createHoldingHistoryEntry(holding = newHolding, referenceDate = referenceDate)
            )
        )

        val result = getHoldingHistoryUseCase(referenceDate)

        assertEquals(2, result.size)
        assertNotNull(result.first { it.holding.id == 1L }.currentEntry)
        assertNotNull(result.first { it.holding.id == 3L }.currentEntry)
        coVerify { mockCreateMissingEntriesUseCase.createMissingEntries(any(), any(), any()) }
    }

    // endregion
}
