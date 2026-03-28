package com.eferraz.entities.goals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GrowthRateTest {

    /**
     * Growth rate over two periods between 1000 and 1400.
     */
    @Test
    fun `GIVEN initial and final values over two periods WHEN calculate THEN returns expected rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1400.0,
            periods = 2,
        )

        // THEN
        assertEquals(18.32, result.percentValue, 0.01)
        assertEquals(0.1832, result.decimalValue, 0.0001)
    }

    /**
     * Equal initial and final values yield zero growth.
     */
    @Test
    fun `GIVEN equal initial and final values WHEN calculate THEN returns zero`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1000.0,
            periods = 5,
        )

        // THEN
        assertEquals(0.0, result.percentValue, 0.0001)
        assertEquals(0.0, result.decimalValue, 0.0001)
    }

    /**
     * Decline from initial to final yields negative rate.
     */
    @Test
    fun `GIVEN decline over periods WHEN calculate THEN returns negative rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 800.0,
            periods = 3,
        )

        // THEN
        assertEquals(-7.17, result.percentValue, 0.01)
        assertEquals(-0.0717, result.decimalValue, 0.0001)
    }

    /**
     * Single period growth from 1000 to 1200.
     */
    @Test
    fun `GIVEN one period WHEN calculate THEN returns twenty percent`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1200.0,
            periods = 1,
        )

        // THEN
        assertEquals(20.0, result.percentValue, 0.01)
        assertEquals(0.20, result.decimalValue, 0.0001)
    }

    /**
     * Doubling over twelve periods.
     */
    @Test
    fun `GIVEN doubling over twelve periods WHEN calculate THEN returns expected rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 2000.0,
            periods = 12,
        )

        // THEN
        assertEquals(5.95, result.percentValue, 0.01)
        assertEquals(0.0595, result.decimalValue, 0.0001)
    }

    /**
     * Decimal initial and final values.
     */
    @Test
    fun `GIVEN decimal initial and final values WHEN calculate THEN returns expected rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1234.56,
            finalValue = 1500.00,
            periods = 3,
        )

        // THEN
        assertEquals(6.71, result.percentValue, 0.01)
        assertEquals(0.0671, result.decimalValue, 0.0001)
    }

    /**
     * Zero initial value is invalid.
     */
    @Test
    fun `GIVEN zero initial value WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 0.0,
                finalValue = 1000.0,
                periods = 2,
            )
        }

        // THEN
        assertEquals("initialValue deve ser maior que 0. Valor recebido: 0.0", exception.message)
    }

    /**
     * Negative initial value is invalid.
     */
    @Test
    fun `GIVEN negative initial value WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = -100.0,
                finalValue = 1000.0,
                periods = 2,
            )
        }

        // THEN
        assertEquals("initialValue deve ser maior que 0. Valor recebido: -100.0", exception.message)
    }

    /**
     * Zero final value is invalid.
     */
    @Test
    fun `GIVEN zero final value WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 0.0,
                periods = 2,
            )
        }

        // THEN
        assertEquals("finalValue deve ser maior que 0. Valor recebido: 0.0", exception.message)
    }

    /**
     * Negative final value is invalid.
     */
    @Test
    fun `GIVEN negative final value WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = -500.0,
                periods = 2,
            )
        }

        // THEN
        assertEquals("finalValue deve ser maior que 0. Valor recebido: -500.0", exception.message)
    }

    /**
     * Zero periods is invalid.
     */
    @Test
    fun `GIVEN zero periods WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 1500.0,
                periods = 0,
            )
        }

        // THEN
        assertEquals("periods deve ser maior ou igual a 1. Valor recebido: 0", exception.message)
    }

    /**
     * Negative periods is invalid.
     */
    @Test
    fun `GIVEN negative periods WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 1500.0,
                periods = -5,
            )
        }

        // THEN
        assertEquals("periods deve ser maior ou igual a 1. Valor recebido: -5", exception.message)
    }

    /**
     * Large growth over few periods.
     */
    @Test
    fun `GIVEN very large growth over four periods WHEN calculate THEN returns expected rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 100.0,
            finalValue = 10000.0,
            periods = 4,
        )

        // THEN
        assertEquals(216.23, result.percentValue, 0.01)
        assertEquals(2.1623, result.decimalValue, 0.0001)
    }

    /**
     * Tiny growth over one period.
     */
    @Test
    fun `GIVEN tiny growth over one period WHEN calculate THEN returns expected rate`() {

        // WHEN
        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1001.0,
            periods = 1,
        )

        // THEN
        assertEquals(0.1, result.percentValue, 0.01)
        assertEquals(0.001, result.decimalValue, 0.0001)
    }
}
