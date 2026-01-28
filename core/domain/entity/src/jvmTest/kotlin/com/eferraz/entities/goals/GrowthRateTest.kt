package com.eferraz.entities.goals

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class GrowthRateTest {

    @Test
    fun `deve calcular taxa de crescimento corretamente`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1400.0,
            periods = 2
        )

        assertEquals(18.32, result.percentValue, 0.01)
        assertEquals(0.1832, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve retornar zero para valores iguais`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1000.0,
            periods = 5
        )

        assertEquals(0.0, result.percentValue, 0.0001)
        assertEquals(0.0, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve calcular taxa negativa para decrescimento`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 800.0,
            periods = 3
        )

        assertEquals(-7.17, result.percentValue, 0.01)
        assertEquals(-0.0717, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve calcular corretamente para um periodo`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1200.0,
            periods = 1
        )

        assertEquals(20.0, result.percentValue, 0.01)
        assertEquals(0.20, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve calcular corretamente para muitos periodos`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 2000.0,
            periods = 12
        )

        assertEquals(5.95, result.percentValue, 0.01)
        assertEquals(0.0595, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve calcular corretamente com valores decimais`() {

        val result = GrowthRate.calculate(
            initialValue = 1234.56,
            finalValue = 1500.00,
            periods = 3
        )

        // Taxa = (1500/1234.56)^(1/3) - 1 ≈ 6.71%
        assertEquals(6.71, result.percentValue, 0.01)
        assertEquals(0.0671, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve lancar excecao para valor inicial zero`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 0.0,
                finalValue = 1000.0,
                periods = 2
            )
        }

        assertEquals("initialValue deve ser maior que 0. Valor recebido: 0.0", exception.message)
    }

    @Test
    fun `deve lancar excecao para valor inicial negativo`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = -100.0,
                finalValue = 1000.0,
                periods = 2
            )
        }

        assertEquals("initialValue deve ser maior que 0. Valor recebido: -100.0", exception.message)
    }

    @Test
    fun `deve lancar excecao para valor final zero`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 0.0,
                periods = 2
            )
        }

        assertEquals("finalValue deve ser maior que 0. Valor recebido: 0.0", exception.message)
    }

    @Test
    fun `deve lancar excecao para valor final negativo`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = -500.0,
                periods = 2
            )
        }

        assertEquals("finalValue deve ser maior que 0. Valor recebido: -500.0", exception.message)
    }

    @Test
    fun `deve lancar excecao para periodos zero`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 1500.0,
                periods = 0
            )
        }

        assertEquals("periods deve ser maior ou igual a 1. Valor recebido: 0", exception.message)
    }

    @Test
    fun `deve lancar excecao para periodos negativos`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GrowthRate.calculate(
                initialValue = 1000.0,
                finalValue = 1500.0,
                periods = -5
            )
        }

        assertEquals("periods deve ser maior ou igual a 1. Valor recebido: -5", exception.message)
    }

    @Test
    fun `deve calcular crescimento muito alto corretamente`() {

        val result = GrowthRate.calculate(
            initialValue = 100.0,
            finalValue = 10000.0,
            periods = 4
        )

        assertEquals(216.23, result.percentValue, 0.01)
        assertEquals(2.1623, result.decimalValue, 0.0001)
    }

    @Test
    fun `deve calcular crescimento muito pequeno corretamente`() {

        val result = GrowthRate.calculate(
            initialValue = 1000.0,
            finalValue = 1001.0,
            periods = 1
        )

        assertEquals(0.1, result.percentValue, 0.01)
        assertEquals(0.001, result.decimalValue, 0.0001)
    }
}
