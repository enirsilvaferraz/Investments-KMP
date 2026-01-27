package com.eferraz.entities.transactions

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.entities.transactions.VariableIncomeTransaction
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

    @Test
    fun `deve retornar zeros para lista vazia de transacoes`() {

        val result = TransactionBalance.calculate(emptyList<VariableIncomeTransaction>())

        assertEquals(0.0, result.contributions, 0.001)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(0.0, result.balance, 0.001)
    }

    @Test
    fun `deve calcular contribuicoes corretamente para compras de renda variavel`() {

        // Compra 1: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 2: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 3: 30 ações × R$ 58,00 = R$ 1.740,00
        // Total: R$ 7.376,00
        val result = TransactionBalance.calculate(
            listOf(
                createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
                createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
                createVariableIncomeTransaction(TransactionType.PURCHASE, 30.0, 58.0)
            )
        )

        assertEquals(7376.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(7376.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular balanco corretamente para compras e vendas de renda variavel`() {

        // Compra 1: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 2: 50 ações × R$ 56,36 = R$ 2.818,00
        // Compra 3: 30 ações × R$ 58,00 = R$ 1.740,00
        // Venda:    10 ações × R$ 60,00 = R$ 600,00
        // Balanço: R$ 7.376,00 - R$ 600,00 = R$ 6.776,00
        val result = TransactionBalance.calculate(
            listOf(
                createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
                createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 56.36),
                createVariableIncomeTransaction(TransactionType.PURCHASE, 30.0, 58.0),
                createVariableIncomeTransaction(TransactionType.SALE, 10.0, 60.0)
            )
        )

        assertEquals(7376.00, result.contributions, 0.01)
        assertEquals(600.00, result.withdrawals, 0.01)
        assertEquals(6776.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular contribuicoes corretamente para compras de renda fixa`() {

        // Compra 1: R$ 5.000,00
        // Compra 2: R$ 3.000,00
        // Compra 3: R$ 2.000,00
        // Total: R$ 10.000,00
        val result = TransactionBalance.calculate(
            listOf(
                createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
                createFixedIncomeTransaction(TransactionType.PURCHASE, 3000.0),
                createFixedIncomeTransaction(TransactionType.PURCHASE, 2000.0)
            )
        )

        assertEquals(10000.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(10000.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular balanco negativo corretamente para compras e resgates de renda fixa`() {

        // Compra 1: R$ 5.000,00
        // Compra 2: R$ 3.000,00
        // Compra 3: R$ 2.000,00
        // Resgate:  R$ 11.500,00
        // Balanço: R$ 10.000,00 - R$ 11.500,00 = -R$ 1.500,00
        val result = TransactionBalance.calculate(
            listOf(
                createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
                createFixedIncomeTransaction(TransactionType.PURCHASE, 3000.0),
                createFixedIncomeTransaction(TransactionType.PURCHASE, 2000.0),
                createFixedIncomeTransaction(TransactionType.SALE, 11500.0)
            )
        )

        assertEquals(10000.00, result.contributions, 0.01)
        assertEquals(11500.00, result.withdrawals, 0.01)
        assertEquals(-1500.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular contribuicoes corretamente para compras de fundos`() {

        // Compra 1: R$ 10.000,00
        // Compra 2: R$ 5.000,00
        // Compra 3: R$ 8.000,00
        // Compra 4: R$ 7.000,00
        // Total: R$ 30.000,00
        val result = TransactionBalance.calculate(
            listOf(
                createFundsTransaction(TransactionType.PURCHASE, 10000.0),
                createFundsTransaction(TransactionType.PURCHASE, 5000.0),
                createFundsTransaction(TransactionType.PURCHASE, 8000.0),
                createFundsTransaction(TransactionType.PURCHASE, 7000.0)
            )
        )

        assertEquals(30000.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(30000.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular balanco corretamente para compras e resgates de fundos`() {

        // Compra 1: R$ 10.000,00
        // Compra 2: R$ 5.000,00
        // Compra 3: R$ 8.000,00
        // Compra 4: R$ 7.000,00
        // Resgate:  R$ 12.000,00
        // Balanço: R$ 30.000,00 - R$ 12.000,00 = R$ 18.000,00
        val result = TransactionBalance.calculate(
            listOf(
                createFundsTransaction(TransactionType.PURCHASE, 10000.0),
                createFundsTransaction(TransactionType.PURCHASE, 5000.0),
                createFundsTransaction(TransactionType.PURCHASE, 8000.0),
                createFundsTransaction(TransactionType.PURCHASE, 7000.0),
                createFundsTransaction(TransactionType.SALE, 12000.0)
            )
        )

        assertEquals(30000.00, result.contributions, 0.01)
        assertEquals(12000.00, result.withdrawals, 0.01)
        assertEquals(18000.00, result.balance, 0.01)
    }

    @Test
    fun `deve retornar balanco negativo quando houver apenas vendas`() {

        // Venda: 100 ações × R$ 50,00 = R$ 5.000,00
        // Balanço: R$ 0,00 - R$ 5.000,00 = -R$ 5.000,00
        val result = TransactionBalance.calculate(
            listOf(
                createVariableIncomeTransaction(TransactionType.SALE, 100.0, 50.0)
            )
        )

        assertEquals(0.0, result.contributions, 0.001)
        assertEquals(5000.00, result.withdrawals, 0.01)
        assertEquals(-5000.00, result.balance, 0.01)
    }

    @Test
    fun `deve calcular balanco corretamente para tipos mistos de ativos`() {

        // Ações:  50 × R$ 20,00 = R$ 1.000,00
        // CDB:    R$ 5.000,00
        // Fundo:  R$ 3.000,00
        // Venda:  10 × R$ 25,00 = R$ 250,00
        // Balanço: R$ 9.000,00 - R$ 250,00 = R$ 8.750,00
        val result = TransactionBalance.calculate(
            listOf(
                createVariableIncomeTransaction(TransactionType.PURCHASE, 50.0, 20.0),
                createFixedIncomeTransaction(TransactionType.PURCHASE, 5000.0),
                createFundsTransaction(TransactionType.PURCHASE, 3000.0),
                createVariableIncomeTransaction(TransactionType.SALE, 10.0, 25.0)
            )
        )

        assertEquals(9000.00, result.contributions, 0.01)
        assertEquals(250.00, result.withdrawals, 0.01)
        assertEquals(8750.00, result.balance, 0.01)
    }
}
