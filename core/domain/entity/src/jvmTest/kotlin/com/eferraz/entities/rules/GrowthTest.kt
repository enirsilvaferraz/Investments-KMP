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
import org.junit.Test

class GrowthTest {

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
            every { referenceDate } returns this@GrowthTest.referenceDate
        }
    }

    // --- 1. Calculation Logic Tests ---

    @Test
    fun `GIVEN pure appreciation without contributions WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 1: Valorização Pura (sem aportes)
        // Início: 1000, Fim: 1100, Transações: 0
        // Apreciação: 100 (10%)
        // Aportes: 0, Retiradas: 0
        // Crescimento: 100 + 0 - 0 = 100
        // %: 100 / 1000 = 10%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1100.0)

        // WHEN
        val result = Growth.calculate(current, previous, emptyList())

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN appreciation with contribution WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 2: Aporte com Valorização
        // Início: 1000, Aporte: 500, Fim: 1600 (1000 + 500 + 100 ganho)
        // Apreciação: 100 (6.66% sobre 1500)
        // Aportes: 500, Retiradas: 0
        // Crescimento: 100 + 500 - 0 = 600
        // %: 600 / 1000 = 60%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1600.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        assertEquals(600.0, result.value, 0.001)
        assertEquals(60.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN appreciation with withdrawal WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 3: Retirada com Valorização
        // Início: 1000, Venda: 200, Fim: 900 (1000 - 200 + 100 ganho)
        // Apreciação: 100 (10%)
        // Aportes: 0, Retiradas: 200
        // Crescimento: 100 + 0 - 200 = -100
        // %: -100 / 1000 = -10%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 900.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 200.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN only contributions without appreciation WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 4: Apenas Aportes (sem lucro)
        // Início: 1000, Aporte: 500, Fim: 1500 (sem variação de preço)
        // Apreciação: 0 (0%)
        // Aportes: 500, Retiradas: 0
        // Crescimento: 0 + 500 - 0 = 500
        // %: 500 / 1000 = 50%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1500.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        assertEquals(500.0, result.value, 0.001)
        assertEquals(50.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN loss with contributions WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 5: Prejuízo com Aportes
        // Início: 1000, Aporte: 500, Fim: 1400 (1000 + 500 - 100 prejuízo)
        // Apreciação: -100 (-6.66%)
        // Aportes: 500, Retiradas: 0
        // Crescimento: -100 + 500 - 0 = 400
        // %: 400 / 1000 = 40%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1400.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 500.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        assertEquals(400.0, result.value, 0.001)
        assertEquals(40.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN profit with partial withdrawal WHEN calculating THEN should return correct growth and percentage`() {
        // GIVEN
        // Exemplo 6: Lucro com Retirada Parcial
        // Início: 1000, Venda: 300, Fim: 850 (1000 - 300 + 150 ganho)
        // Apreciação: 150 (15%)
        // Aportes: 0, Retiradas: 300
        // Crescimento: 150 + 0 - 300 = -150
        // %: -150 / 1000 = -15%
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 850.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.SALE, 300.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        assertEquals(-150.0, result.value, 0.001)
        assertEquals(-15.0, result.percentage, 0.001)
    }

    // --- 2. Edge Cases Tests ---

    @Test
    fun `GIVEN first month without previous history WHEN calculating THEN should return correct growth and zero percentage`() {
        // GIVEN
        // Exemplo 7: Primeiro Mês (sem histórico anterior)
        // Início: 0 (sem histórico), Aporte: 1000, Fim: 1000
        // Apreciação: 0 (0% - primeiro mês)
        // Aportes: 1000, Retiradas: 0
        // Crescimento: 0 + 1000 - 0 = 1000
        // %: 0% (base = 0, retorna 0%)
        val current = createMockHistory(holding, 1000.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 1000.0)
        )

        // WHEN
        val result = Growth.calculate(current, previousHistory = null, transactions)

        // THEN
        assertEquals(1000.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN first month without previous history and no transactions WHEN calculating THEN should return zero growth`() {
        // GIVEN
        // Regra de Exceção: Primeiro mês sem transações
        val current = createMockHistory(holding, 1000.0)

        // WHEN
        val result = Growth.calculate(current, previousHistory = null, emptyList())

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN previous value is zero but has contributions WHEN calculating percentage THEN should use contributions as base`() {
        // GIVEN
        // Base zero mas há aportes: usa aportes como base (regra effectiveBase)
        val previous = createMockHistory(holding, 0.0)
        val current = createMockHistory(holding, 100.0)
        val transactions = listOf(
            createMockTransaction(holding, TransactionType.PURCHASE, 100.0)
        )

        // WHEN
        val result = Growth.calculate(current, previous, transactions)

        // THEN
        // growthValue = appreciation (0) + balance (100) = 100
        // effectiveBase = contributions (100)
        // percentage = 100 / 100 * 100 = 100%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(100.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN previous value is zero and no contributions WHEN calculating percentage THEN should return zero percentage`() {
        // GIVEN
        // Base zero e sem aportes: retorna 0% para evitar divisão por zero
        val previous = createMockHistory(holding, 0.0)
        val current = createMockHistory(holding, 100.0)

        // WHEN
        val result = Growth.calculate(current, previous, emptyList())

        // THEN
        // growthValue = appreciation (100) + balance (0) = 100
        // effectiveBase = 0 (sem previousValue e sem contributions)
        // percentage = 0%
        assertEquals(100.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    // --- 3. Alternative Method Tests (with balance and appreciation) ---

    @Test
    fun `GIVEN balance and appreciation WHEN calculating with alternative method THEN should return correct growth`() {
        // GIVEN
        val previous = createMockHistory(holding, 1000.0)
        val current = createMockHistory(holding, 1600.0)
        val balance = TransactionBalance.calculate(
            listOf(createMockTransaction(holding, TransactionType.PURCHASE, 500.0))
        )
        val appreciation = Appreciation.calculate(current, previous, balance)

        // WHEN
        val result = Growth.calculate(previous, balance, appreciation)

        // THEN
        // Crescimento: 100 + 500 - 0 = 600
        // %: 600 / 1000 = 60%
        assertEquals(600.0, result.value, 0.001)
        assertEquals(60.0, result.percentage, 0.001)
    }
}
