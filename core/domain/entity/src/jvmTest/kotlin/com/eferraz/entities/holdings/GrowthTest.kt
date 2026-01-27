package com.eferraz.entities.holdings

import org.junit.Assert
import org.junit.Test

class GrowthTest {

    @Test
    fun `deve calcular crescimento corretamente com valorizacao pura sem contribuicoes`() {

        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(100.0, result.value, 0.001)
        Assert.assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente com valorizacao e contribuicao`() {

        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(600.0, result.value, 0.001)
        Assert.assertEquals(60.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente com valorizacao e resgate`() {

        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 100.0
        )

        Assert.assertEquals(-100.0, result.value, 0.001)
        Assert.assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente com apenas contribuicoes e valorizacao`() {

        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1500.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(500.0, result.value, 0.001)
        Assert.assertEquals(50.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente com perda e contribuicoes`() {

        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(400.0, result.value, 0.001)
        Assert.assertEquals(40.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente com lucro e resgate parcial`() {

        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 850.0,
            contributions = 0.0,
            withdrawals = 150.0
        )

        Assert.assertEquals(-150.0, result.value, 0.001)
        Assert.assertEquals(-15.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente no primeiro mes sem valor anterior`() {

        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1000.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(0.0, result.value, 0.001)
        Assert.assertEquals(0.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento corretamente no primeiro mes com desvalorizacao`() {

        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 900.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(-100.0, result.value, 0.001)
        Assert.assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `deve calcular crescimento negativo corretamente`() {

        val result = Growth.calculate(
            previousValue = 2000.0,
            currentValue = 1500.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(-500.0, result.value, 0.001)
        Assert.assertEquals(-25.0, result.percentage, 0.001)
    }

    @Test
    fun `deve retornar zero quando nao houver valor anterior nem contribuicoes`() {

        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        Assert.assertEquals(0.0, result.value, 0.001)
        Assert.assertEquals(0.0, result.percentage, 0.001)
    }
}