package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createStockQuoteHistory
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.repositories.StockQuoteHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CreateHistoryUseCaseTest {

    private lateinit var mockHoldingHistoryRepository: HoldingHistoryRepository
    private lateinit var mockQuoteHistoryRepository: StockQuoteHistoryRepository
    private lateinit var createHistoryUseCase: CreateHistoryUseCase

    @BeforeTest
    fun setup() {
        mockHoldingHistoryRepository = mockk<HoldingHistoryRepository>(relaxed = true)
        mockQuoteHistoryRepository = mockk<StockQuoteHistoryRepository>(relaxed = true)

        createHistoryUseCase = CreateHistoryUseCase(
            holdingHistoryRepository = mockHoldingHistoryRepository,
            quoteHistoryRepository = mockQuoteHistoryRepository,
            context = Dispatchers.Unconfined // Para testes síncronos
        )
    }

    // region Teste 1: FixedIncomeAsset - Retorna histórico anterior

    @Test
    fun `GIVEN FixedIncomeAsset with previous history THEN return previous history entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = fixedIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 1000.0
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 0) {
            mockQuoteHistoryRepository.getQuote(any())
        }
    }

    // endregion

    // region Teste 2: FixedIncomeAsset - Sem histórico anterior

    @Test
    fun `GIVEN FixedIncomeAsset without previous history THEN return default entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = fixedIncomeAsset)

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 0) {
            mockQuoteHistoryRepository.getQuote(any())
        }
    }

    // endregion

    // region Teste 3: InvestmentFundAsset - Retorna histórico anterior

    @Test
    fun `GIVEN InvestmentFundAsset with previous history THEN return previous history entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val investmentFundAsset = createInvestmentFundAsset()
        val holding = createAssetHolding(id = 1, asset = investmentFundAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 2000.0
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 0) {
            mockQuoteHistoryRepository.getQuote(any())
        }
    }

    // endregion

    // region Teste 4: InvestmentFundAsset - Sem histórico anterior

    @Test
    fun `GIVEN InvestmentFundAsset without previous history THEN return default entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val investmentFundAsset = createInvestmentFundAsset()
        val holding = createAssetHolding(id = 1, asset = investmentFundAsset)

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 0) {
            mockQuoteHistoryRepository.getQuote(any())
        }
    }

    // endregion

    // region Teste 5: VariableIncomeAsset - Caso feliz com close

    @Test
    fun `GIVEN VariableIncomeAsset with previous history and quote with close THEN return new entry with calculated values`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "PETR4"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 100.0,
            endOfMonthQuantity = 10.0
        )
        val quoteHistory = createStockQuoteHistory(
            ticker = ticker,
            close = 52.0,
            adjustedClose = 50.0
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry
        coEvery {
            mockQuoteHistoryRepository.getQuote(ticker)
        } returns quoteHistory

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(52.0, result.endOfMonthValue)
        assertEquals(10.0, result.endOfMonthQuantity)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 1) {
            mockQuoteHistoryRepository.getQuote(ticker)
        }
    }

    // endregion

    // region Teste 6: VariableIncomeAsset - Usa adjustedClose quando close é null

    @Test
    fun `GIVEN VariableIncomeAsset with quote having null close but adjustedClose THEN return new entry with adjustedClose`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "VALE3"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 80.0,
            endOfMonthQuantity = 5.0
        )
        val quoteHistory = createStockQuoteHistory(
            ticker = ticker,
            close = null,
            adjustedClose = 45.0
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry
        coEvery {
            mockQuoteHistoryRepository.getQuote(ticker)
        } returns quoteHistory

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(45.0, result.endOfMonthValue)
        assertEquals(5.0, result.endOfMonthQuantity)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 1) {
            mockQuoteHistoryRepository.getQuote(ticker)
        }
    }

    // endregion

    // region Teste 7: VariableIncomeAsset - Sem histórico anterior

    @Test
    fun `GIVEN VariableIncomeAsset without previous history THEN return default entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "ITUB4"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 0) {
            mockQuoteHistoryRepository.getQuote(any())
        }
    }

    // endregion

    // region Teste 8: VariableIncomeAsset - Sem close nem adjustedClose

    @Test
    fun `GIVEN VariableIncomeAsset with quote having null close and adjustedClose THEN return default entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "BBDC4"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 60.0,
            endOfMonthQuantity = 8.0
        )
        val quoteHistory = createStockQuoteHistory(
            ticker = ticker,
            close = null,
            adjustedClose = null
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry
        coEvery {
            mockQuoteHistoryRepository.getQuote(ticker)
        } returns quoteHistory

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 1) {
            mockQuoteHistoryRepository.getQuote(ticker)
        }
    }

    // endregion

    // region Teste 9: Verificar que o mês anterior é calculado corretamente

    @Test
    fun `GIVEN any asset type THEN previous month is calculated correctly`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.JANUARY)
        val expectedPreviousDate = YearMonth(2023, Month.DECEMBER)
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = fixedIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = expectedPreviousDate
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(expectedPreviousDate, holding)
        } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(expectedPreviousDate, holding)
        }
    }

    // endregion

    // region Teste 10: VariableIncomeAsset - Preserva quantidade do mês anterior

    @Test
    fun `GIVEN VariableIncomeAsset THEN preserve previous month quantity in new entry`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "MGLU3"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)
        val previousQuantity = 15.5
        val previousEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = previousDate,
            endOfMonthValue = 200.0,
            endOfMonthQuantity = previousQuantity
        )
        val quoteHistory = createStockQuoteHistory(
            ticker = ticker,
            close = 30.0
        )

        coEvery {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        } returns previousEntry
        coEvery {
            mockQuoteHistoryRepository.getQuote(ticker)
        } returns quoteHistory

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousQuantity, result.endOfMonthQuantity)
        assertEquals(30.0, result.endOfMonthValue)
        coVerify(exactly = 1) {
            mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding)
        }
        coVerify(exactly = 1) {
            mockQuoteHistoryRepository.getQuote(ticker)
        }
    }

    // endregion
}
