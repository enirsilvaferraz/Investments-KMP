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

    private val otherHolding = mockk<AssetHolding> {
        every { id } returns 2L
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

    // --- 1. Validation Tests ---

    @Test
    fun `GIVEN all inputs belong to the same holding WHEN calculating THEN should return valid result`() {

        // GIVEN
        val currentHistory = createMockHistory(holding, 1000.0)
        val previousHistory = createMockHistory(holding, 900.0)
        val transaction = createMockTransaction(holding, TransactionType.PURCHASE, 100.0)

        // WHEN
        val result = Appreciation.calculate(
            currentHistory = currentHistory,
            previousHistory = previousHistory,
            transactions = listOf(transaction)
        )

        // THEN
        assertTrue(result.value.isFinite())
    }

    @Test
    fun `GIVEN current history belongs to another holding WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentHistory = createMockHistory(otherHolding, 1000.0)
        val previousHistory = createMockHistory(holding, 900.0)

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            Appreciation.calculate(
                currentHistory = currentHistory,
                previousHistory = previousHistory,
                transactions = emptyList()
            )
        }
    }

    @Test
    fun `GIVEN previous history belongs to another holding WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentHistory = createMockHistory(holding, 1000.0)
        val previousHistory = createMockHistory(otherHolding, 900.0)

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            Appreciation.calculate(
                currentHistory = currentHistory,
                previousHistory = previousHistory,
                transactions = emptyList()
            )
        }
    }

    @Test
    fun `GIVEN a transaction belongs to another holding WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentHistory = createMockHistory(holding, 1000.0)
        val transaction = createMockTransaction(otherHolding, TransactionType.PURCHASE, 100.0)

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            Appreciation.calculate(
                currentHistory = currentHistory,
                previousHistory = null,
                transactions = listOf(transaction)
            )
        }
    }

    // --- 2. Calculation Logic Tests ---

    @Test
    fun `GIVEN holding appreciation without transactions WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        // Início: 1000, Fim: 1100, Transações: 0
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1100.0)

        // WHEN
        val result = Appreciation.calculate( current, previous, emptyList())

        // THEN
        // Resultado: 1100 - 1000 - 0 = 100
        // %: 100 / 1000 = 10%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding appreciation with purchase transaction WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        // Início: 1000, Aporte: 500, Fim: 1600
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1600.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        // Resultado: 1600 - 1000 - 500 = 100
        // Base: 1000 + 500 = 1500
        // %: 100 / 1500 = 6.666...%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(6.666, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding appreciation with sale transaction WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        // Início: 1000, Venda: 200, Fim: 900
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 900.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 200.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        // Resultado: 900 - 1000 - (-200) = 100
        // Base: 1000
        // %: 100 / 1000 = 10%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN day trade transaction resulting in profit WHEN calculating THEN should return correct profit and percentage`() {

        // GIVEN
        // Início: 0, Compra: 1000, Venda: 1100, Fim: 0
        val current = createMockHistory(holding, 0.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 1000.0),
            createMockTransaction(holding, TransactionType.SALE, 1100.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, transactions)

        // THEN
        // Resultado: 0 - 0 - (-100) = 100
        // Base: 0 + 1000 = 1000
        // %: 100 / 1000 = 10%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation without transactions WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        // Início: 1000, Fim: 900
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 900.0)

        // WHEN
        val result = Appreciation.calculate( current, previous, emptyList())

        // THEN
        // Resultado: 900 - 1000 - 0 = -100
        // %: -100 / 1000 = -10%
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation with purchase transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        // Início: 1000, Aporte: 500, Fim: 1300
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1300.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        // Resultado: 1300 - 1000 - 500 = -200
        // Base: 1000 + 500 = 1500
        // %: -200 / 1500 = -13.333%
        assertEquals(-200.0, result.value, 0.001)
        assertEquals(-13.333, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation with sale transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // GIVEN
        // Início: 1000, Venda: 200, Fim: 700
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 700.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 200.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previous, transactions)

        // THEN
        // Resultado: 700 - 1000 - (-200) = -100
        // Base: 1000
        // %: -100 / 1000 = -10%
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    // --- 3. Edge Cases Tests ---

    @Test
    fun `GIVEN first month calculation (no history) WHEN calculating THEN should use zero as starting value`() {

        // GIVEN
        // Início: 0 (sem histórico), Compra: 500, Fim: 550
        val current = createMockHistory(holding, 550.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, transactions)

        // THEN
        // Resultado: 550 - 0 - 500 = 50
        // Base: 0 + 500 = 500
        // %: 50 / 500 = 10%
        assertEquals(50.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN base value is zero or negative WHEN calculating THEN should return zero percent roi`() {

        // GIVEN
        // Início: 0, Fim: 0, Sem transações
        val current = createMockHistory(holding, 0.0)

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, emptyList())

        // THEN
        // Resultado: 0
        // Base: 0
        // %: 0.0 (regra)
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN first month calculation (no history) and no transactions WHEN calculating THEN should return zero result`() {

        // GIVEN
        val current = createMockHistory(holding, 1000.0)

        // WHEN
        val result = Appreciation.calculate( current, previousHistory = null, emptyList())

        // THEN
        // Não há histórico anterior e não há transações. Assume-se implantação de saldo.
        // Resultado: 0.0
        // %: 0.0
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }
}
