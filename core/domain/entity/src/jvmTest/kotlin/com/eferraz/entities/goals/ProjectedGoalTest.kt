package com.eferraz.entities.goals

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFailsWith

class ProjectedGoalTest {

    @Test
    fun `deve calcular valor projetado corretamente com valor atual, aporte e rentabilidade`() {

        val result = ProjectedGoal.calculate(
            currentValue = 10000.0,
            appreciationRate = 0.80,
            contribution = 1500.0
        )

        Assert.assertEquals(11592.0, result.value, 0.01)
    }

    @Test
    fun `deve calcular valor projetado corretamente apenas com rentabilidade`() {

        val result = ProjectedGoal.calculate(
            currentValue = 50000.0,
            appreciationRate = 1.0,
            contribution = 0.0
        )

        Assert.assertEquals(50500.0, result.value, 0.01)
    }

    @Test
    fun `deve calcular valor projetado corretamente apenas com aporte`() {

        val result = ProjectedGoal.calculate(
            currentValue = 5000.0,
            appreciationRate = 0.0,
            contribution = 2000.0
        )

        Assert.assertEquals(7000.0, result.value, 0.01)
    }

    @Test
    fun `deve calcular valor projetado corretamente com valor atual zero`() {

        val result = ProjectedGoal.calculate(
            currentValue = 0.0,
            appreciationRate = 0.80,
            contribution = 1500.0
        )

        Assert.assertEquals(1512.0, result.value, 0.01)
    }

    @Test
    fun `deve retornar zero quando todos os valores forem zero`() {

        val result = ProjectedGoal.calculate(
            currentValue = 0.0,
            appreciationRate = 0.0,
            contribution = 0.0
        )

        Assert.assertEquals(0.0, result.value, 0.001)
    }

    @Test
    fun `deve lancar excecao quando valor atual for negativo`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = -1000.0,
                appreciationRate = 0.80,
                contribution = 1500.0
            )
        }

        Assert.assertEquals("O valor atual deve ser maior que zero. Valor recebido: -1000.0", exception.message)
    }

    @Test
    fun `deve lancar excecao quando taxa de rentabilidade for negativa`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = 10000.0,
                appreciationRate = -0.80,
                contribution = 1500.0
            )
        }

        Assert.assertEquals("A taxa de rentabilidade deve ser maior que zero. Valor recebido: -0.8", exception.message)
    }

    @Test
    fun `deve lancar excecao quando aporte mensal for negativo`() {

        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = 10000.0,
                appreciationRate = 0.80,
                contribution = -1500.0
            )
        }

        Assert.assertEquals("O aporte deve ser maior que zero. Valor recebido: -1500.0", exception.message)
    }

    @Test
    fun `deve lancar excecao para valor atual quando todos os valores forem negativos`() {

        // Deve falhar na primeira validação (currentValue)
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = -10000.0,
                appreciationRate = -0.80,
                contribution = -1500.0
            )
        }

        Assert.assertEquals("O valor atual deve ser maior que zero. Valor recebido: -10000.0", exception.message)
    }
}