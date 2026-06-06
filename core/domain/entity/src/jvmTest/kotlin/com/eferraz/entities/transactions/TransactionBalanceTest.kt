package com.eferraz.entities.transactions

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionBalanceTest {

    private fun createTransaction(
        type: TransactionType,
        quantity: Double,
        unitPrice: Double,
        date: LocalDate = LocalDate(2025, 1, 15),
    ): AssetTransaction =
        AssetTransaction(
            id = 0L,
            date = date,
            type = type,
            quantity = quantity,
            unitPrice = unitPrice,
        )

    private fun createTotalValueTransaction(
        type: TransactionType,
        totalValue: Double,
        date: LocalDate = LocalDate(2025, 1, 10),
    ): AssetTransaction =
        AssetTransaction(
            id = 0L,
            date = date,
            type = type,
            quantity = 1.0,
            unitPrice = totalValue,
        )

    /**
     * Empty transaction list yields zero contributions, withdrawals and balance.
     */
    @Test
    fun `GIVEN empty transaction list WHEN calculate THEN returns zeros`() {

        // WHEN
        val result = TransactionBalance.calculate(emptyList())

        // THEN
        assertEquals(0.0, result.contributions, 0.001)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(0.0, result.balance, 0.001)
    }

    /**
     * Three variable-income purchases sum contributions and balance.
     */
    @Test
    fun `GIVEN variable income purchases WHEN calculate THEN sums contributions`() {

        // GIVEN
        val transactions = listOf(
            createTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createTransaction(TransactionType.PURCHASE, 30.0, 58.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(7376.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(7376.00, result.balance, 0.01)
    }

    /**
     * Purchases and one sale reduce net balance.
     */
    @Test
    fun `GIVEN purchases and sale WHEN calculate THEN net balance matches flows`() {

        // GIVEN
        val transactions = listOf(
            createTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createTransaction(TransactionType.PURCHASE, 50.0, 56.36),
            createTransaction(TransactionType.PURCHASE, 30.0, 58.0),
            createTransaction(TransactionType.SALE, 10.0, 60.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(7376.00, result.contributions, 0.01)
        assertEquals(600.00, result.withdrawals, 0.01)
        assertEquals(6776.00, result.balance, 0.01)
    }

    /**
     * Fixed income purchases only (qty=1, unitPrice=total).
     */
    @Test
    fun `GIVEN fixed income purchases WHEN calculate THEN sums contributions`() {

        // GIVEN
        val transactions = listOf(
            createTotalValueTransaction(TransactionType.PURCHASE, 5000.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 3000.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 2000.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(10000.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(10000.00, result.balance, 0.01)
    }

    /**
     * Fixed income purchases and large redemption yield negative balance.
     */
    @Test
    fun `GIVEN fixed income purchases and redemption WHEN calculate THEN negative balance`() {

        // GIVEN
        val transactions = listOf(
            createTotalValueTransaction(TransactionType.PURCHASE, 5000.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 3000.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 2000.0),
            createTotalValueTransaction(TransactionType.SALE, 11500.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(10000.00, result.contributions, 0.01)
        assertEquals(11500.00, result.withdrawals, 0.01)
        assertEquals(-1500.00, result.balance, 0.01)
    }

    /**
     * Fund purchases only (qty=1, unitPrice=total).
     */
    @Test
    fun `GIVEN fund purchases WHEN calculate THEN sums contributions`() {

        // GIVEN
        val transactions = listOf(
            createTotalValueTransaction(TransactionType.PURCHASE, 10000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 5000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 8000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 7000.0, LocalDate(2025, 1, 5)),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(30000.00, result.contributions, 0.01)
        assertEquals(0.0, result.withdrawals, 0.001)
        assertEquals(30000.00, result.balance, 0.01)
    }

    /**
     * Fund purchases and one redemption.
     */
    @Test
    fun `GIVEN fund purchases and redemption WHEN calculate THEN net balance`() {

        // GIVEN
        val transactions = listOf(
            createTotalValueTransaction(TransactionType.PURCHASE, 10000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 5000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 8000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.PURCHASE, 7000.0, LocalDate(2025, 1, 5)),
            createTotalValueTransaction(TransactionType.SALE, 12000.0, LocalDate(2025, 1, 5)),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(30000.00, result.contributions, 0.01)
        assertEquals(12000.00, result.withdrawals, 0.01)
        assertEquals(18000.00, result.balance, 0.01)
    }

    /**
     * Only sales with no purchases yield negative balance.
     */
    @Test
    fun `GIVEN only sales WHEN calculate THEN negative balance`() {

        // GIVEN
        val transactions = listOf(
            createTransaction(TransactionType.SALE, 100.0, 50.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(0.0, result.contributions, 0.001)
        assertEquals(5000.00, result.withdrawals, 0.01)
        assertEquals(-5000.00, result.balance, 0.01)
    }

    /**
     * Mixed asset classes in one list (unified model: RV qty×price, RF/Funds qty=1).
     */
    @Test
    fun `GIVEN mixed asset transaction types WHEN calculate THEN aggregates correctly`() {

        // GIVEN
        val transactions = listOf(
            createTransaction(TransactionType.PURCHASE, 50.0, 20.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 5000.0),
            createTotalValueTransaction(TransactionType.PURCHASE, 3000.0, LocalDate(2025, 1, 5)),
            createTransaction(TransactionType.SALE, 10.0, 25.0),
        )

        // WHEN
        val result = TransactionBalance.calculate(transactions)

        // THEN
        assertEquals(9000.00, result.contributions, 0.01)
        assertEquals(250.00, result.withdrawals, 0.01)
        assertEquals(8750.00, result.balance, 0.01)
    }
}
