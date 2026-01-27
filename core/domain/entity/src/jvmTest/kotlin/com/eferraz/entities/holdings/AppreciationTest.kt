package com.eferraz.entities.holdings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppreciationTest {

    @Test
    fun `deve calcular valorizacao corretamente sem transacoes`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular valorizacao corretamente com transacao de compra`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular valorizacao corretamente com transacao de venda`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 200.0
        )

        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular valorizacao corretamente em day trade com lucro`() {

        val result = Appreciation.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 1000.0,
            withdrawals = 1100.0
        )

        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular desvalorizacao corretamente sem transacoes`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular desvalorizacao corretamente com transacao de compra`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular desvalorizacao corretamente com transacao de venda`() {

        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 700.0,
            contributions = 0.0,
            withdrawals = 200.0
        )

        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve retornar zero quando valor anterior for zero e sem transacoes`() {

        val result = Appreciation.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }
}
