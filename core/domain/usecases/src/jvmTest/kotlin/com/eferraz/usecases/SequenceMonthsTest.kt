package com.eferraz.usecases

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for [SequenceMonths].
 */
class SequenceMonthsTest {

    /**
     * Inclusive range from start to end builds three months.
     */
    @Test
    fun `GIVEN valid start and end months WHEN build THEN returns full sequence`() {

        // GIVEN
        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        // WHEN
        val months = SequenceMonths.build(start, end)

        // THEN
        assertEquals(3, months.size)
        assertEquals(YearMonth(2024, 1), months[0])
        assertEquals(YearMonth(2024, 2), months[1])
        assertEquals(YearMonth(2024, 3), months[2])
    }

    /**
     * Same start and end yields a single month.
     */
    @Test
    fun `GIVEN same start and end month WHEN build THEN returns single element`() {

        // GIVEN
        val start = YearMonth(2024, 6)
        val end = YearMonth(2024, 6)

        // WHEN
        val months = SequenceMonths.build(start, end)

        // THEN
        assertEquals(1, months.size)
        assertEquals(YearMonth(2024, 6), months[0])
    }

    /**
     * Year boundary is included in the sequence.
     */
    @Test
    fun `GIVEN range crossing year boundary WHEN build THEN includes all months`() {

        // GIVEN
        val start = YearMonth(2023, 11)
        val end = YearMonth(2024, 2)

        // WHEN
        val months = SequenceMonths.build(start, end)

        // THEN
        assertEquals(4, months.size)
        assertEquals(YearMonth(2023, 11), months[0])
        assertEquals(YearMonth(2023, 12), months[1])
        assertEquals(YearMonth(2024, 1), months[2])
        assertEquals(YearMonth(2024, 2), months[3])
    }

    /**
     * Custom maxMonths allows full span up to DEFAULT_MAX_MONTHS.
     */
    @Test
    fun `GIVEN span at default max months WHEN build with maxMonths THEN returns full length`() {

        // GIVEN
        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS - 1, DateTimeUnit.MONTH)

        // WHEN
        val months = SequenceMonths.build(start, end, maxMonths = 120)

        // THEN
        assertEquals(120, months.size)
        assertEquals(YearMonth(2020, 1), months.first())
        assertEquals(YearMonth(2029, 12), months.last())
    }

    /**
     * Explicit maxMonths larger than span returns span length.
     */
    @Test
    fun `GIVEN maxMonths larger than span WHEN build THEN returns span length`() {

        // GIVEN
        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 5)
        val maxMonths = 5

        // WHEN
        val months = SequenceMonths.build(start, end, maxMonths)

        // THEN
        assertEquals(5, months.size)
    }

    /**
     * Default maxMonths allows 120-month span.
     */
    @Test
    fun `GIVEN span of default max length WHEN build THEN returns one hundred twenty months`() {

        // GIVEN
        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS - 1, DateTimeUnit.MONTH)

        // WHEN
        val months = SequenceMonths.build(start, end)

        // THEN
        assertEquals(120, months.size)
    }

    /**
     * Start after end is invalid.
     */
    @Test
    fun `GIVEN start after end WHEN build THEN throws`() {

        // GIVEN
        val start = YearMonth(2024, 6)
        val end = YearMonth(2024, 3)

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end)
        }

        // THEN
        assertEquals(
            "O mês inicial (2024-06) não pode ser posterior ao mês final (2024-03)",
            exception.message,
        )
    }

    /**
     * Span longer than default max without raising maxMonths throws.
     */
    @Test
    fun `GIVEN span exceeding default max WHEN build THEN throws`() {

        // GIVEN
        val start = YearMonth(2020, 1)
        val end = start.plus(SequenceMonths.DEFAULT_MAX_MONTHS, DateTimeUnit.MONTH)

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end)
        }

        // THEN
        assertEquals(
            "O intervalo entre 2020-01 e 2030-01 excede o limite de 120 meses. Total de meses calculados: 121",
            exception.message,
        )
    }

    /**
     * maxMonths zero is invalid.
     */
    @Test
    fun `GIVEN maxMonths zero WHEN build THEN throws`() {

        // GIVEN
        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end, maxMonths = 0)
        }

        // THEN
        assertEquals("maxMonths deve ser maior que zero. Valor recebido: 0", exception.message)
    }

    /**
     * maxMonths negative is invalid.
     */
    @Test
    fun `GIVEN maxMonths negative WHEN build THEN throws`() {

        // GIVEN
        val start = YearMonth(2024, 1)
        val end = YearMonth(2024, 3)

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            SequenceMonths.build(start, end, maxMonths = -1)
        }

        // THEN
        assertEquals("maxMonths deve ser maior que zero. Valor recebido: -1", exception.message)
    }
}
