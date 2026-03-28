package com.eferraz.entities.holdings

import kotlin.test.Test
import kotlin.test.assertEquals

class AppreciationTest {

    /**
     * Pure market move without cash flows.
     */
    @Test
    fun `GIVEN previous and current values without transactions WHEN calculate THEN returns appreciation`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * Purchase between months is stripped from the move.
     */
    @Test
    fun `GIVEN purchase contribution WHEN calculate THEN isolates market appreciation`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * Withdrawal between months is stripped from the move.
     */
    @Test
    fun `GIVEN withdrawal WHEN calculate THEN isolates market appreciation`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 200.0,
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * Day trade profit: contributions and withdrawals with zero carry values.
     */
    @Test
    fun `GIVEN day trade profit with zero carry WHEN calculate THEN returns appreciation on flow`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 1000.0,
            withdrawals = 1100.0,
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * Pure decline without cash flows.
     */
    @Test
    fun `GIVEN decline without transactions WHEN calculate THEN returns negative appreciation`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * Decline with purchase masks part of the loss.
     */
    @Test
    fun `GIVEN decline with purchase WHEN calculate THEN isolates market move`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * Decline with withdrawal masks part of the loss.
     */
    @Test
    fun `GIVEN decline with withdrawal WHEN calculate THEN isolates market move`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 700.0,
            contributions = 0.0,
            withdrawals = 200.0,
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * Zero previous and current with no flows.
     */
    @Test
    fun `GIVEN zero previous and current without flows WHEN calculate THEN returns zero`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }
}
