package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createStockQuoteHistory
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.strategies.CopyHistoryStrategy
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CreateHistoryUseCaseTest {

    private lateinit var mockHoldingHistoryRepository: HoldingHistoryRepository
    private lateinit var mockGetQuotesUseCase: GetQuotesUseCase
    private lateinit var createHistoryUseCase: CreateHistoryUseCase

    @BeforeTest
    fun setup() {
        mockHoldingHistoryRepository = mockk<HoldingHistoryRepository>(relaxed = true)
        mockGetQuotesUseCase = mockk<GetQuotesUseCase>(relaxed = true)

        val strategies = listOf(
            CopyHistoryStrategy.FixedIncomeHistoryStrategy(mockHoldingHistoryRepository),
            CopyHistoryStrategy.VariableIncomeHistoryStrategy(mockHoldingHistoryRepository, mockGetQuotesUseCase)
        )

        createHistoryUseCase = CreateHistoryUseCase(
            strategies = strategies,
            repository = mockHoldingHistoryRepository,
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 1000.0)

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(previousEntry) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }
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

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }

        // Verifica que o registro vazio foi persistido (conforme regra de negócio)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 2000.0)

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(previousEntry) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }
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

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }

        // Verifica que o registro vazio foi persistido (conforme regra de negócio)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 100.0, endOfMonthQuantity = 10.0)
        val quoteHistory = createStockQuoteHistory(ticker = ticker, close = 52.0, adjustedClose = 50.0)

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns null
        // Depois verifica o mês anterior
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(52.0, result.endOfMonthValue)
        assertEquals(10.0, result.endOfMonthQuantity)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 80.0, endOfMonthQuantity = 5.0)
        val quoteHistory = createStockQuoteHistory(ticker = ticker, close = null, adjustedClose = 45.0)

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns null
        // Depois verifica o mês anterior
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(45.0, result.endOfMonthValue)
        assertEquals(5.0, result.endOfMonthQuantity)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
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
        val quoteHistory = createStockQuoteHistory(ticker = ticker, close = 25.0)

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns null
        // Depois verifica o mês anterior
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns null
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
        // A estratégia verifica primeiro o mês de referência, depois o anterior
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }

        // Quando não há histórico anterior, a estratégia retorna null e o use case cria registro vazio
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 8: VariableIncomeAsset - Sem close nem adjustedClose

    @Test
    fun `GIVEN VariableIncomeAsset with quote having null close and adjustedClose THEN throw exception`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "BBDC4"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 60.0, endOfMonthQuantity = 8.0)
        val quoteHistory = createStockQuoteHistory(ticker = ticker, close = null, adjustedClose = null)

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns null
        // Depois verifica o mês anterior
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding))

        // Assert
        // A estratégia lança exceção quando não há close nem adjustedClose
        // O use case propaga a exceção como Result.failure()
        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalStateException)
        assertEquals("Quote history is missing close or adjustedClose", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }
        // Quando há exceção, o registro não é persistido
        coVerify(exactly = 0) { mockHoldingHistoryRepository.upsert(any()) }
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = expectedPreviousDate)

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(expectedPreviousDate, holding) } returns previousEntry

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousEntry, result)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(expectedPreviousDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(previousEntry) }
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
        val previousEntry = createHoldingHistoryEntry(id = 1, holding = holding, referenceDate = previousDate, endOfMonthValue = 200.0, endOfMonthQuantity = previousQuantity)
        val quoteHistory = createStockQuoteHistory(ticker = ticker, close = 30.0)

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns null
        // Depois verifica o mês anterior
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) } returns previousEntry
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(previousQuantity, result.endOfMonthQuantity)
        assertEquals(30.0, result.endOfMonthValue)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 11: Data limite - Data igual a Out/2025

    @Test
    fun `GIVEN referenceDate equals October 2025 THEN return empty entry and persist it`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2025, Month.OCTOBER)
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = fixedIncomeAsset)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(0.0, result.endOfMonthValue)
        assertEquals(1.0, result.endOfMonthQuantity)
        assertEquals(0.0, result.endOfMonthAverageCost)
        assertEquals(0.0, result.totalInvested)
        assertNull(result.id)

        // Verifica que nenhuma estratégia foi chamada
        coVerify(exactly = 0) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(any(), any()) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }

        // Verifica que o registro vazio foi persistido
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 12: Data limite - Data anterior a Out/2025

    @Test
    fun `GIVEN referenceDate before October 2025 THEN return empty entry and persist it`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2025, Month.SEPTEMBER)
        val variableIncomeAsset = createVariableIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(0.0, result.endOfMonthValue)
        assertEquals(1.0, result.endOfMonthQuantity)
        assertEquals(0.0, result.endOfMonthAverageCost)
        assertEquals(0.0, result.totalInvested)
        assertNull(result.id)

        // Verifica que nenhuma estratégia foi chamada
        coVerify(exactly = 0) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(any(), any()) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }

        // Verifica que o registro vazio foi persistido
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 13: Validação - Holding ID inválido

    @Test
    fun `GIVEN holding with invalid ID THEN throw IllegalArgumentException`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 0, asset = fixedIncomeAsset) // ID inválido

        // Act & Assert
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding))

        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Holding ID deve ser maior que zero", result.exceptionOrNull()?.message)

        // Verifica que nenhuma operação foi executada
        coVerify(exactly = 0) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(any(), any()) }
        coVerify(exactly = 0) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 14: VariableIncomeAsset - Histórico já existe para o mês

    @Test
    fun `GIVEN VariableIncomeAsset with existing history for reference month THEN update only endOfMonthValue`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()
        val ticker = "PETR4"
        val variableIncomeAsset = createVariableIncomeAsset(ticker = ticker)
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)

        // Histórico existente para o mês de referência
        val existingEntry = createHoldingHistoryEntry(
            id = 1,
            holding = holding,
            referenceDate = referenceDate,
            endOfMonthValue = 50.0, // Valor antigo
            endOfMonthQuantity = 10.0,
            endOfMonthAverageCost = 10.0,
            totalInvested = 100.0
        )

        val quoteHistory = createStockQuoteHistory(
            ticker = ticker,
            close = 55.0, // Novo valor
            adjustedClose = 54.0
        )

        // A estratégia primeiro verifica se existe histórico para o mês de referência
        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) } returns existingEntry
        coEvery { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) } returns Result.success(quoteHistory)

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(55.0, result.endOfMonthValue) // Valor atualizado
        assertEquals(10.0, result.endOfMonthQuantity) // Quantidade preservada
        assertEquals(10.0, result.endOfMonthAverageCost) // Custo médio preservado
        assertEquals(100.0, result.totalInvested) // Total investido preservado
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding) }
        coVerify(exactly = 0) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(previousDate, holding) }
        coVerify(exactly = 1) { mockGetQuotesUseCase(GetQuotesUseCase.Params(ticker, referenceDate)) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion

    // region Teste 16: Valores padrão em registros vazios

    @Test
    fun `GIVEN empty history entry THEN has default values according to business rules`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val fixedIncomeAsset = createFixedIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = fixedIncomeAsset)

        coEvery { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(any(), any()) } returns null

        // Act
        val result = createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert - Valores padrão conforme RN 4.1
        assertEquals(0.0, result.endOfMonthValue, "Valor de mercado deve ser 0,00")
        assertEquals(1.0, result.endOfMonthQuantity, "Quantidade deve ser 1,00")
        assertEquals(0.0, result.endOfMonthAverageCost, "Custo médio deve ser 0,00")
        assertEquals(0.0, result.totalInvested, "Valor investido deve ser 0,00")
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertNull(result.id)
    }

    // endregion

    // region Teste 17: Nenhuma estratégia disponível (fallback)

    @Test
    fun `GIVEN no strategies available THEN return empty entry and persist it`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val variableIncomeAsset = createVariableIncomeAsset()
        val holding = createAssetHolding(id = 1, asset = variableIncomeAsset)

        // Criar use case sem estratégias
        val useCaseWithoutStrategies = CreateHistoryUseCase(
            strategies = emptyList(), // Lista vazia de estratégias
            repository = mockHoldingHistoryRepository,
            context = Dispatchers.Unconfined
        )

        // Act
        val result = useCaseWithoutStrategies(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

        // Assert
        assertEquals(holding, result.holding)
        assertEquals(referenceDate, result.referenceDate)
        assertEquals(0.0, result.endOfMonthValue)
        assertEquals(1.0, result.endOfMonthQuantity)
        assertEquals(0.0, result.endOfMonthAverageCost)
        assertEquals(0.0, result.totalInvested)
        assertNull(result.id)

        // Verifica que nenhuma estratégia foi chamada
        coVerify(exactly = 0) { mockHoldingHistoryRepository.getByHoldingAndReferenceDate(any(), any()) }
        coVerify(exactly = 0) { mockGetQuotesUseCase(any()) }

        // Verifica que o registro vazio foi persistido (conforme RN 4.4)
        coVerify(exactly = 1) { mockHoldingHistoryRepository.upsert(any()) }
    }

    // endregion
}
