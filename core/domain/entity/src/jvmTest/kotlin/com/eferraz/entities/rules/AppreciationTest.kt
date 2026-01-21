package com.eferraz.entities.rules

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.TransactionType
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class AppreciationTest {

    private val referenceDate = YearMonth(2025, Month.JANUARY)

    // Mock dependencies
    private val holding = mockk<AssetHolding> {
        every { id } returns 1L
    }

    // Helper to create mocked transactions
    private fun createMockTransaction(
        mockHolding: AssetHolding,
        type: TransactionType,
        value: Double,
        date: LocalDate = LocalDate(2025, 1, 1)
    ): AssetTransaction {
        return mockk {
            every { holding } returns mockHolding
            every { this@mockk.type } returns type
            every { totalValue } returns value
            every { this@mockk.date } returns date
            every { id } returns 0L // Irrelevant for most tests
        }
    }

    // Helper to create mocked history
    private fun createMockHistory(
        mockHolding: AssetHolding,
        endValue: Double
    ): HoldingHistoryEntry {
        return mockk {
            every { holding } returns mockHolding
            every { endOfMonthValue } returns endValue
            every { referenceDate } returns this@AppreciationTest.referenceDate
        }
    }

    // --- 2. Calculation Logic Tests ---

    /**
     * #### Exemplo 1: Valorização Pura
     *   - Início: 1000
     *   - Fim: 1100
     *   - Balanço das Transações: 0
     *   - Resultado: 1100 - 1000 - 0 = **100**
     *   - %: 100 / 1000 = **10%**
     */
    @Test
    fun `GIVEN holding appreciation without transactions WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1100.0)

        // WHEN
        val result = Appreciation.calculate( current, previous, emptyList())

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 2: Aporte com Valorização
     *   - Início: 1000
     *   - Aporte: 500
     *   - Fim: 1600
     *   - Balanço das Transações: +500
     *   - Resultado: 1600 - 1000 - 500 = **100**
     *   - Base: 1000 + 500 = 1500
     *   - %: 100 / 1500 = **6.66%**
     */
    @Test
    fun `GIVEN holding appreciation with purchase transaction WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1600.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(6.666, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 3: Retirada com Valorização
     *   - Início: 1000
     *   - Venda: 200
     *   - Fim: 900
     *   - Balanço das Transações: -200
     *   - Resultado: 900 - 1000 - (-200) = **100**
     *   - Base: 1000 - 200 = 800 (capital exposto ao risco após retirada)
     *   - %: 100 / 800 = **12.5%**
     */
    @Test
    fun `GIVEN holding appreciation with sale transaction WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 900.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 200.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(12.5, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 4: Encerramento com Lucro (Day Trade)
     *   - Início: 0
     *   - Compra: 1000
     *   - Venda: 1100
     *   - Fim: 0
     *   - Balanço das Transações: 1000 - 1100 = -100
     *   - Resultado: 0 - 0 - (-100) = **100**
     *   - Base: 0 + 1000 = 1000
     *   - %: 100 / 1000 = **10%**
     */
    @Test
    fun `GIVEN day trade transaction resulting in profit WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        val current = createMockHistory(holding, 0.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 1000.0),
            createMockTransaction(holding, TransactionType.SALE, 1100.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, transactions)

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 5: Depreciação Pura
     *   - Início: 1000
     *   - Fim: 900
     *   - Balanço das Transações: 0
     *   - Resultado: 900 - 1000 - 0 = **-100**
     *   - %: -100 / 1000 = **-10%**
     */
    @Test
    fun `GIVEN holding depreciation without transactions WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 900.0)

        // WHEN
        val result = Appreciation.calculate( current, previous, emptyList())

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 6: Aporte com Depreciação
     *   - Início: 1000
     *   - Aporte: 500
     *   - Fim: 1300
     *   - Balanço das Transações: +500
     *   - Resultado: 1300 - 1000 - 500 = **-200**
     *   - Base: 1000 + 500 = 1500
     *   - %: -200 / 1500 = **-13.333%**
     */
    @Test
    fun `GIVEN holding depreciation with purchase transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1300.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        assertEquals(-200.0, result.value, 0.001)
        assertEquals(-13.333, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 7: Retirada com Depreciação
     *   - Início: 1000
     *   - Venda: 200
     *   - Fim: 700
     *   - Balanço das Transações: -200
     *   - Resultado: 700 - 1000 - (-200) = **-100**
     *   - Base: 1000 - 200 = 800 (capital exposto ao risco após retirada)
     *   - %: -100 / 800 = **-12.5%**
     */
    @Test
    fun `GIVEN holding depreciation with sale transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 700.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 200.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-12.5, result.percentage, 0.001)
    }

    // --- 3. Edge Cases Tests ---

    /**
     * #### Exemplo 8: Primeiro Mês (Sem Histórico Anterior)
     *   - Início: 0 (sem histórico)
     *   - Compra: 500
     *   - Fim: 550
     *   - Balanço das Transações: +500
     *   - Resultado: 550 - 0 - 500 = **50**
     *   - Base: 0 + 500 = 500
     *   - %: 50 / 500 = **10%**
     */
    @Test
    fun `GIVEN first month calculation (no history) WHEN calculating THEN should use zero as starting value`() {

        // GIVEN
        val current = createMockHistory(holding, 550.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, transactions)

        // THEN
        assertEquals(50.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 9: Base Zero (Sem Histórico e Sem Transações)
     *   - Início: 0 (sem histórico)
     *   - Fim: 0
     *   - Balanço das Transações: 0
     *   - Resultado: 0 - 0 - 0 = **0**
     *   - Base: 0
     *   - %: **0%** (regra: para evitar divisão por zero)
     */
    @Test
    fun `GIVEN base value is zero or negative WHEN calculating THEN should return zero percent roi`() {

        // GIVEN
        val current = createMockHistory(holding, 0.0)

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, emptyList())

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    /**
     * #### Exemplo 10: Primeiro Mês Sem Transações (Saldo Inicial Implantado)
     *   - Início: 0 (sem histórico)
     *   - Fim: 1000
     *   - Balanço das Transações: 0
     *   - Resultado: **0** (regra: assume-se implantação de saldo, não ganho espontâneo)
     *   - %: **0%**
     */
    @Test
    fun `GIVEN first month calculation (no history) and no transactions WHEN calculating THEN should return zero result`() {

        // GIVEN
        val current = createMockHistory(holding, 1000.0)

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, emptyList())

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }
}
