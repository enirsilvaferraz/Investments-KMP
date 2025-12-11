package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MergeHistoryUseCaseTest {

    private lateinit var mockAssetHoldingRepository: AssetHoldingRepository
    private lateinit var mockHoldingHistoryRepository: HoldingHistoryRepository
    private lateinit var mockCreateHistoryUseCase: CreateHistoryUseCase
    private lateinit var mergeHistoryUseCase: MergeHistoryUseCase

    @BeforeTest
    fun setup() {
        mockAssetHoldingRepository = mockk<AssetHoldingRepository>(relaxed = true)
        mockHoldingHistoryRepository = mockk<HoldingHistoryRepository>(relaxed = true)
        mockCreateHistoryUseCase = mockk<CreateHistoryUseCase>(relaxed = true)

        mergeHistoryUseCase = MergeHistoryUseCase(
            holdingHistoryRepository = mockHoldingHistoryRepository,
            assetHoldingRepository = mockAssetHoldingRepository,
            createHistoryUseCase = mockCreateHistoryUseCase,
            context = Dispatchers.Unconfined // Para testes síncronos
        )
    }

    // region Teste 1: Caso Feliz - Histórico Completo

    @Test
    fun `GIVEN holdings with complete history THEN return results with current and previous entries`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2, asset = createFixedIncomeAsset())

        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 150.0)
        val previousEntry1 = createHoldingHistoryEntry(id = 2, holding = holding1, referenceDate = previousDate, endOfMonthValue = 100.0)

        val currentEntry2 = createHoldingHistoryEntry(id = 3, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val previousEntry2 = createHoldingHistoryEntry(id = 4, holding = holding2, referenceDate = previousDate, endOfMonthValue = 180.0)

        val holdings = listOf(holding1, holding2)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
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

        // Verificar chamadas
        coVerify(exactly = 1) { mockAssetHoldingRepository.getAll() }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(previousDate) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), any()) }

        // Verificar ordem preservada
        assertEquals(holding1.id, result[0].holding.id)
        assertEquals(holding2.id, result[1].holding.id)
    }

    // endregion

    // region Teste 2: Holdings sem Histórico Anterior

    @Test
    fun `GIVEN holdings without previous history THEN return results with default previousEntry`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2)

        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = createHoldingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)

        val holdings = listOf(holding1, holding2)
        val defaultPreviousEntry1 = createHoldingHistoryEntry(holding = holding1, referenceDate = previousDate)
        val defaultPreviousEntry2 = createHoldingHistoryEntry(holding = holding2, referenceDate = previousDate)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()
        coEvery { mockCreateHistoryUseCase(previousDate, holding1) } returns defaultPreviousEntry1
        coEvery { mockCreateHistoryUseCase(previousDate, holding2) } returns defaultPreviousEntry2

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
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

        // Verificar que createHistoryUseCase foi chamado para criar previousEntry
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding1) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding2) }
    }

    // endregion

    // region Teste 3: Holdings sem Histórico Atual

    @Test
    fun `GIVEN holdings without current history THEN return results with default currentEntry`() = runTest {

        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2)

        val previousEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = createHoldingHistoryEntry(id = 2, holding = holding2, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2)
        val defaultCurrentEntry1 = createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val defaultCurrentEntry2 = createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)
        coEvery { mockCreateHistoryUseCase(referenceDate, holding1) } returns defaultCurrentEntry1
        coEvery { mockCreateHistoryUseCase(referenceDate, holding2) } returns defaultCurrentEntry2

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
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

        // Verificar que createHistoryUseCase foi chamado para criar currentEntry
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding1) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding2) }
    }

    // endregion

    // region Teste 4: Holdings sem Histórico Nenhum

    @Test
    fun `GIVEN holdings without any history THEN return results with default entries`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2)
        val holding3 = createAssetHolding(id = 3, asset = createInvestmentFundAsset())

        val holdings = listOf(holding1, holding2, holding3)
        val defaultCurrentEntry1 = createHoldingHistoryEntry(holding = holding1, referenceDate = referenceDate)
        val defaultCurrentEntry2 = createHoldingHistoryEntry(holding = holding2, referenceDate = referenceDate)
        val defaultCurrentEntry3 = createHoldingHistoryEntry(holding = holding3, referenceDate = referenceDate)
        val defaultPreviousEntry1 = createHoldingHistoryEntry(holding = holding1, referenceDate = previousDate)
        val defaultPreviousEntry2 = createHoldingHistoryEntry(holding = holding2, referenceDate = previousDate)
        val defaultPreviousEntry3 = createHoldingHistoryEntry(holding = holding3, referenceDate = previousDate)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()
        coEvery { mockCreateHistoryUseCase(referenceDate, holding1) } returns defaultCurrentEntry1
        coEvery { mockCreateHistoryUseCase(referenceDate, holding2) } returns defaultCurrentEntry2
        coEvery { mockCreateHistoryUseCase(referenceDate, holding3) } returns defaultCurrentEntry3
        coEvery { mockCreateHistoryUseCase(previousDate, holding1) } returns defaultPreviousEntry1
        coEvery { mockCreateHistoryUseCase(previousDate, holding2) } returns defaultPreviousEntry2
        coEvery { mockCreateHistoryUseCase(previousDate, holding3) } returns defaultPreviousEntry3

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
        assertEquals(3, result.size)

        result.forEach { resultItem ->
            assertTrue(resultItem.holding.id in listOf(1L, 2L, 3L))
            assertEquals(referenceDate, resultItem.currentEntry.referenceDate)
            assertEquals(resultItem.holding, resultItem.currentEntry.holding)
            assertEquals(previousDate, resultItem.previousEntry.referenceDate)
            assertEquals(resultItem.holding, resultItem.previousEntry.holding)
        }

        // Verificar que createHistoryUseCase foi chamado duas vezes por holding
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding1) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding2) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding3) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding1) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding2) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding3) }
    }

    // endregion

    // region Teste 5: Lista Vazia de Holdings

    @Test
    fun `GIVEN empty holdings list THEN return empty list`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holdings = emptyList<AssetHolding>()

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns emptyList()
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns emptyList()

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
        assertTrue(result.isEmpty())

        // Verificar que os repositórios são chamados mesmo sem holdings
        coVerify(exactly = 1) { mockAssetHoldingRepository.getAll() }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) }
        coVerify(exactly = 1) { mockHoldingHistoryRepository.getByReferenceDate(previousDate) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), any()) }
    }

    // endregion

    // region Teste 6: Verificar Ordem Preservada dos Holdings

    @Test
    fun `GIVEN multiple holdings THEN return results in same order as repository`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2)
        val holding3 = createAssetHolding(id = 3)
        val holding4 = createAssetHolding(id = 4)

        val holdings = listOf(holding1, holding2, holding3, holding4)
        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = createHoldingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)
        val currentEntry3 = createHoldingHistoryEntry(id = 3, holding = holding3, referenceDate = referenceDate)
        val currentEntry4 = createHoldingHistoryEntry(id = 4, holding = holding4, referenceDate = referenceDate)

        val previousEntry1 = createHoldingHistoryEntry(id = 5, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = createHoldingHistoryEntry(id = 6, holding = holding2, referenceDate = previousDate)
        val previousEntry3 = createHoldingHistoryEntry(id = 7, holding = holding3, referenceDate = previousDate)
        val previousEntry4 = createHoldingHistoryEntry(id = 8, holding = holding4, referenceDate = previousDate)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2, currentEntry3, currentEntry4)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2, previousEntry3, previousEntry4)

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
        assertEquals(4, result.size)
        assertEquals(holding1.id, result[0].holding.id)
        assertEquals(holding2.id, result[1].holding.id)
        assertEquals(holding3.id, result[2].holding.id)
        assertEquals(holding4.id, result[3].holding.id)

        // Verificar que a ordem corresponde exatamente à ordem dos holdings
        result.forEachIndexed { index, resultItem ->
            assertEquals(holdings[index].id, resultItem.holding.id)
        }
    }

    // endregion

    // region Teste 7: Holdings com Histórico Parcial

    @Test
    fun `GIVEN holdings with partial history THEN return correct results and call createHistoryUseCase only when needed`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        // Holding 1: histórico completo
        val holding1 = createAssetHolding(id = 1)
        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 150.0)
        val previousEntry1 = createHoldingHistoryEntry(id = 2, holding = holding1, referenceDate = previousDate, endOfMonthValue = 100.0)

        // Holding 2: apenas histórico atual
        val holding2 = createAssetHolding(id = 2)
        val currentEntry2 = createHoldingHistoryEntry(id = 3, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val defaultPreviousEntry2 = createHoldingHistoryEntry(holding = holding2, referenceDate = previousDate)

        // Holding 3: apenas histórico anterior
        val holding3 = createAssetHolding(id = 3)
        val previousEntry3 = createHoldingHistoryEntry(id = 4, holding = holding3, referenceDate = previousDate, endOfMonthValue = 300.0)
        val defaultCurrentEntry3 = createHoldingHistoryEntry(holding = holding3, referenceDate = referenceDate)

        // Holding 4: sem histórico nenhum
        val holding4 = createAssetHolding(id = 4)
        val defaultCurrentEntry4 = createHoldingHistoryEntry(holding = holding4, referenceDate = referenceDate)
        val defaultPreviousEntry4 = createHoldingHistoryEntry(holding = holding4, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2, holding3, holding4)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry3)
        coEvery { mockCreateHistoryUseCase(previousDate, holding2) } returns defaultPreviousEntry2
        coEvery { mockCreateHistoryUseCase(referenceDate, holding3) } returns defaultCurrentEntry3
        coEvery { mockCreateHistoryUseCase(referenceDate, holding4) } returns defaultCurrentEntry4
        coEvery { mockCreateHistoryUseCase(previousDate, holding4) } returns defaultPreviousEntry4

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
        assertEquals(4, result.size)

        // Verificar Holding 1: histórico completo
        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        // Verificar Holding 2: apenas atual
        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(defaultPreviousEntry2, result2.previousEntry)

        // Verificar Holding 3: apenas anterior
        val result3 = result.find { it.holding.id == 3L }
        assertNotNull(result3)
        assertEquals(defaultCurrentEntry3, result3.currentEntry)
        assertEquals(previousEntry3, result3.previousEntry)

        // Verificar Holding 4: sem histórico
        val result4 = result.find { it.holding.id == 4L }
        assertNotNull(result4)
        assertEquals(defaultCurrentEntry4, result4.currentEntry)
        assertEquals(defaultPreviousEntry4, result4.previousEntry)

        // Verificar que createHistoryUseCase foi chamado apenas quando necessário
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), holding1) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding2) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding3) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding4) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding4) }
    }

    // endregion

    // region Teste 8: Múltiplos Holdings com Diferentes Tipos de Ativos

    @Test
    fun `GIVEN holdings with different asset types THEN return correct results for each type`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1) // VariableIncome (default)
        val holding2 = createAssetHolding(id = 2, asset = createFixedIncomeAsset())
        val holding3 = createAssetHolding(id = 3, asset = createInvestmentFundAsset())

        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate, endOfMonthValue = 100.0)
        val currentEntry2 = createHoldingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate, endOfMonthValue = 200.0)
        val currentEntry3 = createHoldingHistoryEntry(id = 3, holding = holding3, referenceDate = referenceDate, endOfMonthValue = 300.0)

        val previousEntry1 = createHoldingHistoryEntry(id = 4, holding = holding1, referenceDate = previousDate, endOfMonthValue = 90.0)
        val previousEntry2 = createHoldingHistoryEntry(id = 5, holding = holding2, referenceDate = previousDate, endOfMonthValue = 180.0)
        val previousEntry3 = createHoldingHistoryEntry(id = 6, holding = holding3, referenceDate = previousDate, endOfMonthValue = 270.0)

        val holdings = listOf(holding1, holding2, holding3)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2, currentEntry3)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2, previousEntry3)

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
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

        // Verificar que createHistoryUseCase não foi chamado (todos têm histórico)
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), any()) }
    }

    // endregion

    // region Teste 9: Verificar Mapeamento Correto do Repositório

    @Test
    fun `GIVEN repository returns entries not matching holdings THEN map correctly and create missing entries`() = runTest {
        // Arrange
        val referenceDate = YearMonth(2024, Month.APRIL)
        val previousDate = referenceDate.minusMonth()

        val holding1 = createAssetHolding(id = 1)
        val holding2 = createAssetHolding(id = 2)
        val holding3 = createAssetHolding(id = 3)

        // Repositório retorna apenas histórico para holding1 e holding2, mas não para holding3
        val currentEntry1 = createHoldingHistoryEntry(id = 1, holding = holding1, referenceDate = referenceDate)
        val currentEntry2 = createHoldingHistoryEntry(id = 2, holding = holding2, referenceDate = referenceDate)
        val previousEntry1 = createHoldingHistoryEntry(id = 3, holding = holding1, referenceDate = previousDate)
        val previousEntry2 = createHoldingHistoryEntry(id = 4, holding = holding2, referenceDate = previousDate)

        val defaultCurrentEntry3 = createHoldingHistoryEntry(holding = holding3, referenceDate = referenceDate)
        val defaultPreviousEntry3 = createHoldingHistoryEntry(holding = holding3, referenceDate = previousDate)

        val holdings = listOf(holding1, holding2, holding3)

        coEvery { mockAssetHoldingRepository.getAll() } returns holdings
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(referenceDate) } returns listOf(currentEntry1, currentEntry2)
        coEvery { mockHoldingHistoryRepository.getByReferenceDate(previousDate) } returns listOf(previousEntry1, previousEntry2)
        coEvery { mockCreateHistoryUseCase(referenceDate, holding3) } returns defaultCurrentEntry3
        coEvery { mockCreateHistoryUseCase(previousDate, holding3) } returns defaultPreviousEntry3

        // Act
        val result = mergeHistoryUseCase(MergeHistoryUseCase.Param(referenceDate)).getOrThrow()

        // Assert
        assertEquals(3, result.size)

        // Verificar que holdings com histórico do repositório foram mapeados corretamente
        val result1 = result.find { it.holding.id == 1L }
        assertNotNull(result1)
        assertEquals(currentEntry1, result1.currentEntry)
        assertEquals(previousEntry1, result1.previousEntry)

        val result2 = result.find { it.holding.id == 2L }
        assertNotNull(result2)
        assertEquals(currentEntry2, result2.currentEntry)
        assertEquals(previousEntry2, result2.previousEntry)

        // Verificar que holding sem histórico no repositório teve entradas criadas
        val result3 = result.find { it.holding.id == 3L }
        assertNotNull(result3)
        assertEquals(defaultCurrentEntry3, result3.currentEntry)
        assertEquals(defaultPreviousEntry3, result3.previousEntry)

        // Verificar que createHistoryUseCase foi chamado apenas para holding3
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), holding1) }
        coVerify(exactly = 0) { mockCreateHistoryUseCase(any(), holding2) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(referenceDate, holding3) }
        coVerify(exactly = 1) { mockCreateHistoryUseCase(previousDate, holding3) }
    }

    // endregion
}

