package com.eferraz.usecases

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Testes para a classe [SequenceMonths].
 */
class SequenceMonthsTest {

    @Test
    fun `deve retornar sequencia correta para intervalo valido`() {

        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        val months = SequenceMonths.build(start, end)

        assertEquals(3, months.size)
        assertEquals(YearMonth(2024, 1), months[0])
        assertEquals(YearMonth(2024, 2), months[1])
        assertEquals(YearMonth(2024, 3), months[2])
    }

    @Test
    fun `deve retornar um unico mes quando intervalo for de um mes`() {

        val start = YearMonth(2024, 6)
        val end = YearMonth(2024, 6)

        val months = SequenceMonths.build(start, end)

        assertEquals(1, months.size)
        assertEquals(YearMonth(2024, 6), months[0])
    }

    @Test
    fun `deve lidar corretamente com transicao de ano`() {

        val start = YearMonth(2023, 11)
        val end = YearMonth(2024, 2)

        val months = SequenceMonths.build(start, end)

        assertEquals(4, months.size)
        assertEquals(YearMonth(2023, 11), months[0])
        assertEquals(YearMonth(2023, 12), months[1])
        assertEquals(YearMonth(2024, 1), months[2])
        assertEquals(YearMonth(2024, 2), months[3])
    }

    @Test
    fun `deve retornar sequencia completa para limite maximo permitido`() {

        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS - 1, DateTimeUnit.MONTH)

        val months = SequenceMonths.build(start, end, maxMonths = 120)

        assertEquals(120, months.size)
        assertEquals(YearMonth(2020, 1), months.first())
        assertEquals(YearMonth(2029, 12), months.last())
    }

    @Test
    fun `deve retornar sequencia completa dentro do limite customizado`() {

        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 5)
        val maxMonths = 5

        val months = SequenceMonths.build(start, end, maxMonths)

        assertEquals(5, months.size)
    }

    @Test
    fun `deve respeitar limite padrao de 120 meses`() {

        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS - 1, DateTimeUnit.MONTH)

        val months = SequenceMonths.build(start, end)

        assertEquals(120, months.size)
    }

    @Test
    fun `deve lancar excecao quando mes inicial for posterior ao mes final`() {

        val start = YearMonth(2024, 6)
        val end = YearMonth(2024, 3)

        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end)
        }

        assertEquals(
            "O mês inicial (2024-06) não pode ser posterior ao mês final (2024-03)",
            exception.message
        )
    }

    @Test
    fun `deve lancar excecao quando intervalo exceder limite padrao`() {

        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS, DateTimeUnit.MONTH)

        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end)
        }

        assertEquals(
            "O intervalo entre 2020-01 e 2030-01 excede o limite de 120 meses. Total de meses calculados: 121",
            exception.message
        )
    }

    @Test
    fun `deve lancar excecao quando maxMonths for zero`() {

        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end, maxMonths = 0)
        }

        assertEquals("maxMonths deve ser maior que zero. Valor recebido: 0", exception.message)
    }

    @Test
    fun `deve lancar excecao quando maxMonths for negativo`() {

        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end, maxMonths = -1)
        }

        assertEquals("maxMonths deve ser maior que zero. Valor recebido: -1", exception.message)
    }
}
