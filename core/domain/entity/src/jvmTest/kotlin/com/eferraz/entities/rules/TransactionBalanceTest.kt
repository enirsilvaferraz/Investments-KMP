package com.eferraz.entities.rules

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeTransaction
import com.eferraz.entities.FundsTransaction
import com.eferraz.entities.TransactionType
import com.eferraz.entities.VariableIncomeTransaction
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionBalanceTest {

    // Mock holding
    private val holding = mockk<AssetHolding> {
        every { id } returns 1L
    }

    // Helper to create variable income transaction
    private fun createVariableIncomeTransaction(
        type: TransactionType,
        quantity: Double,
        unitPrice: Double,
        date: LocalDate = LocalDate(2025, 1, 15)
    ): VariableIncomeTransaction {
        return mockk {
            every { holding } returns this@TransactionBalanceTest.holding
            every { this@mockk.type } returns type
            every { this@mockk.quantity } returns quantity
            every { this@mockk.unitPrice } returns unitPrice
            every { totalValue } returns quantity * unitPrice
            every { this@mockk.date } returns date
            every { id } returns 0L
        }
    }

    // Helper to create fixed income transaction
    private fun createFixedIncomeTransaction(
        type: TransactionType,
        totalValue: Double,
        date: LocalDate = LocalDate(2025, 1, 10)
    ): FixedIncomeTransaction {
        return mockk {
            every { holding } returns this@TransactionBalanceTest.holding
            every { this@mockk.type } returns type
            every { this@mockk.totalValue } returns totalValue
            every { this@mockk.date } returns date
            every { id } returns 0L
        }
    }

    // Helper to create funds transaction
    private fun createFundsTransaction(
        type: TransactionType,
        totalValue: Double,
        date: LocalDate = LocalDate(2025, 1, 5)
    ): FundsTransaction {
        return mockk {
            every { holding } returns this@TransactionBalanceTest.holding
            every { this@mockk.type } returns type
            every { this@mockk.totalValue } returns totalValue
            every { this@mockk.date } returns date
            every { id } returns 0L
        }
    }

    // --- 1. Empty List Tests ---

    @Test
    fun `GIVEN empty transaction list WHEN calculating THEN should return zero values`() {

        // GIVEN
        val transactions = emptyList<VariableIncomeTransaction>()

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(0.0, result.totalContributions, 0.001)
        assertEquals(0.0, result.totalWithdrawals, 0.001)
        assertEquals(0.0, result.balance, 0.001)
    }

    // --- 2. Variable Income Tests (Renda Variável) ---

    @Test
    fun `GIVEN variable income purchases WHEN calculating THEN should return correct contributions`() {

        // GIVEN
        // Compra 1: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 2: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 3: 30 ações × R$ 58,00 = R$ 1.740,00
        // Total: R$ 7.376,00
        val transactions = listOf(
            createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createVariableIncomeTransaction(TransactionType.PURCHASE, 30.0, 58.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(7376.00, result.totalContributions, 0.01)
        assertEquals(0.0, result.totalWithdrawals, 0.001)
        assertEquals(7376.00, result.balance, 0.01)
    }

    @Test
    fun `GIVEN variable income purchases and sales WHEN calculating THEN should return correct balance`() {
        // GIVEN
        // Compra 1: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 2: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 3: 30 ações × R$ 58,00 = R$ 1.740,00
        // Venda:    10 ações × R$ 60,00 = R$ 600,00
        // Balanço: R$ 7.376,00 - R$ 600,00 = R$ 6.776,00
        val transactions = listOf(
            createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createVariableIncomeTransaction(TransactionType.PURCHASE, 30.0, 58.0),
            createVariableIncomeTransaction(TransactionType.SALE, 10.0, 60.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(7376.00, result.totalContributions, 0.01)
        assertEquals(600.00, result.totalWithdrawals, 0.01)
        assertEquals(6776.00, result.balance, 0.01)
    }

    // --- 3. Fixed Income Tests (Renda Fixa) ---

    @Test
    fun `GIVEN fixed income purchases WHEN calculating THEN should return correct contributions`() {
        // GIVEN
        // Compra 1: R$ 5.000,00
        // Compra 2: R$ 3.000,00
        // Compra 3: R$ 2.000,00
        // Total: R$ 10.000,00
        val transactions = listOf(
            createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
            createFixedIncomeTransaction(TransactionType.PURCHASE, 3000.0),
            createFixedIncomeTransaction(TransactionType.PURCHASE, 2000.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(10000.00, result.totalContributions, 0.01)
        assertEquals(0.0, result.totalWithdrawals, 0.001)
        assertEquals(10000.00, result.balance, 0.01)
    }

    @Test
    fun `GIVEN fixed income purchases and sales WHEN calculating THEN should return correct negative balance`() {
        // GIVEN
        // Compra 1: R$ 5.000,00
        // Compra 2: R$ 3.000,00
        // Compra 3: R$ 2.000,00
        // Resgate:  R$ 11.500,00
        // Balanço: R$ 10.000,00 - R$ 11.500,00 = -R$ 1.500,00
        val transactions = listOf(
            createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
            createFixedIncomeTransaction(TransactionType.PURCHASE, 3000.0),
            createFixedIncomeTransaction(TransactionType.PURCHASE, 2000.0),
            createFixedIncomeTransaction(TransactionType.SALE, 11500.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(10000.00, result.totalContributions, 0.01)
        assertEquals(11500.00, result.totalWithdrawals, 0.01)
        assertEquals(-1500.00, result.balance, 0.01)
    }

    // --- 4. Funds Tests (Fundos de Investimento) ---

    @Test
    fun `GIVEN funds purchases WHEN calculating THEN should return correct contributions`() {
        // GIVEN
        // Compra 1: R$ 10.000,00
        // Compra 2: R$ 5.000,00
        // Compra 3: R$ 8.000,00
        // Compra 4: R$ 7.000,00
        // Total: R$ 30.000,00
        val transactions = listOf(
            createFundsTransaction(TransactionType.PURCHASE, 10000.0),
            createFundsTransaction(TransactionType.PURCHASE, 5000.0),
            createFundsTransaction(TransactionType.PURCHASE, 8000.0),
            createFundsTransaction(TransactionType.PURCHASE, 7000.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(30000.00, result.totalContributions, 0.01)
        assertEquals(0.0, result.totalWithdrawals, 0.001)
        assertEquals(30000.00, result.balance, 0.01)
    }

    @Test
    fun `GIVEN funds purchases and sales WHEN calculating THEN should return correct balance`() {
        // GIVEN
        // Compra 1: R$ 10.000,00
        // Compra 2: R$ 5.000,00
        // Compra 3: R$ 8.000,00
        // Compra 4: R$ 7.000,00
        // Resgate:  R$ 12.000,00
        // Balanço: R$ 30.000,00 - R$ 12.000,00 = R$ 18.000,00
        val transactions = listOf(
            createFundsTransaction(TransactionType.PURCHASE, 10000.0),
            createFundsTransaction(TransactionType.PURCHASE, 5000.0),
            createFundsTransaction(TransactionType.PURCHASE, 8000.0),
            createFundsTransaction(TransactionType.PURCHASE, 7000.0),
            createFundsTransaction(TransactionType.SALE, 12000.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(30000.00, result.totalContributions, 0.01)
        assertEquals(12000.00, result.totalWithdrawals, 0.01)
        assertEquals(18000.00, result.balance, 0.01)
    }

    // --- 5. Edge Cases Tests ---

    @Test
    fun `GIVEN only sales WHEN calculating THEN should return negative balance`() {
        // GIVEN
        // Venda: 100 ações × R$ 50,00 = R$ 5.000,00
        // Balanço: R$ 0,00 - R$ 5.000,00 = -R$ 5.000,00
        val transactions = listOf(
            createVariableIncomeTransaction(TransactionType.SALE, 100.0, 50.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(0.0, result.totalContributions, 0.001)
        assertEquals(5000.00, result.totalWithdrawals, 0.01)
        assertEquals(-5000.00, result.balance, 0.01)
    }

    @Test
    fun `GIVEN mixed asset types WHEN calculating THEN should return correct balance`() {
        // GIVEN
        // Ações:  50 × R$ 20,00 = R$ 1.000,00
        // CDB:    R$ 5.000,00
        // Fundo:  R$ 3.000,00
        // Venda:  10 × R$ 25,00 = R$ 250,00
        // Balanço: R$ 9.000,00 - R$ 250,00 = R$ 8.750,00
        val transactions = listOf(
            createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 20.0),
            createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
            createFundsTransaction(TransactionType.PURCHASE, 3000.0),
            createVariableIncomeTransaction(TransactionType.SALE, 10.0, 25.0)
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(9000.00, result.totalContributions, 0.01)
        assertEquals(250.00, result.totalWithdrawals, 0.01)
        assertEquals(8750.00, result.balance, 0.01)
    }
}
