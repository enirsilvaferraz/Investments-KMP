package com.eferraz.entities.goals

import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class GoalProjectionsTest {

    @Test
    fun `deve calcular projecoes mensais corretamente`() {

        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.0,
            contribution = 10.0,
            targetValue = 50.0
        )

        assertEquals(10.0, result.map[YearMonth(2026, Month.JANUARY)]!!.value, 0.01)
        assertEquals(20.0, result.map[YearMonth(2026, Month.FEBRUARY)]!!.value, 0.01)
        assertEquals(30.0, result.map[YearMonth(2026, Month.MARCH)]!!.value, 0.01)
        assertEquals(40.0, result.map[YearMonth(2026, Month.APRIL)]!!.value, 0.01)
        assertEquals(50.0, result.map[YearMonth(2026, Month.MAY)]!!.value, 0.01)
        assertEquals(null, result.map[YearMonth(2026, Month.JUNE)]?.value)
    }

    @Test
    fun `deve comecar no mes inicial`() {

        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.80,
            contribution = 1500.0,
            targetValue = 100_000.0
        )

        val firstMonth = result.map.keys.first()
        assertEquals(YearMonth(2026, Month.JANUARY), firstMonth)
    }

    @Test
    fun `deve parar quando atingir a meta`() {

        val targetValue = 100_000.0
        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.80,
            contribution = 1500.0,
            targetValue = targetValue
        )

        val lastValue = result.map.values.last().value
        assertTrue("Ultimo valor deve ser >= meta", lastValue >= targetValue)

        // Verifica que o penúltimo valor estava abaixo da meta
        val values = result.map.values.toList()
        if (values.size > 1) {
            val penultimateValue = values[values.size - 2].value
            assertTrue("Penultimo valor deve ser < meta", penultimateValue < targetValue)
        }
    }

    @Test
    fun `deve respeitar limite de meses`() {

        val result = GoalProjections.calculate(
            maxMonths = 12,
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.50,
            contribution = 500.0,
            targetValue = 500_000.0 // Meta muito alta para 12 meses
        )

        assertEquals(12, result.map.size)
        assertEquals(YearMonth(2026, Month.DECEMBER), result.map.keys.last())
    }

    @Test
    fun `sem contribuicao e sem rentabilidade deve lancar excecao`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjections.calculate(
                maxMonths = 120,
                initialValue = 0.0,
                startMonth = YearMonth(2026, Month.JANUARY),
                appreciationRate = 0.0,
                contribution = 0.0,
                targetValue = 1000.0
            )
        }

        assertEquals(
            "Contribuição ou taxa de rentabilidade devem ser maiores que zero. " +
            "Valores recebidos: contribution=0.0, appreciationRate=0.0",
            exception.message
        )
    }

    @Test
    fun `deve lancar excecao para maxMonths invalido`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjections.calculate(
                maxMonths = 0,
                initialValue = 0.0,
                startMonth = YearMonth(2026, Month.JANUARY),
                appreciationRate = 1.0,
                contribution = 100.0,
                targetValue = 10_000.0
            )
        }

        // Deve falhar na primeira validação (currentValue)
        assertEquals("maxMonths deve ser maior ou igual a 1. Valor recebido: 0", exception.message)
    }
}
