package com.eferraz.entities.value

import com.eferraz.entities.assets.CNPJ
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class CNPJTest {

    // --- Casos Válidos ---

    @Test
    fun `GIVEN valid CNPJ with mask WHEN creating CNPJ THEN should create successfully`() {
        // GIVEN
        val cnpjWithMask = "12.345.678/0001-90"

        // WHEN
        val cnpj = CNPJ(cnpjWithMask)

        // THEN
        assertEquals(cnpjWithMask, cnpj.get())
    }

    @Test
    fun `GIVEN valid CNPJ without mask WHEN creating CNPJ THEN should create successfully`() {
        // GIVEN
        val cnpjWithoutMask = "12345678000190"

        // WHEN
        val cnpj = CNPJ(cnpjWithoutMask)

        // THEN
        assertEquals(cnpjWithoutMask, cnpj.get())
    }

    @Test
    fun `GIVEN valid CNPJ with mask different digits WHEN creating CNPJ THEN should create successfully`() {
        // GIVEN
        val cnpjWithMask = "11.222.333/4444-55"

        // WHEN
        val cnpj = CNPJ(cnpjWithMask)

        // THEN
        assertEquals(cnpjWithMask, cnpj.get())
    }

    // --- Casos Inválidos ---

    @Test
    fun `GIVEN empty string WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val emptyString = ""

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(emptyString)
        }
    }

    @Test
    fun `GIVEN CNPJ with less than 14 digits WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val shortCnpj = "1234567890123" // 13 dígitos

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(shortCnpj)
        }
    }

    @Test
    fun `GIVEN CNPJ with more than 14 digits WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val longCnpj = "123456789012345" // 15 dígitos

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(longCnpj)
        }
    }

    @Test
    fun `GIVEN CNPJ with incorrect mask format WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val incorrectMask = "12-345-678/0001.90" // máscara incorreta

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(incorrectMask)
        }
    }

    @Test
    fun `GIVEN CNPJ with partial mask WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val partialMask = "12.345.678/0001" // máscara incompleta

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(partialMask)
        }
    }

    @Test
    fun `GIVEN CNPJ with non-numeric characters WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val cnpjWithLetters = "12.345.678/0001-AB" // contém letras

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(cnpjWithLetters)
        }
    }

    @Test
    fun `GIVEN CNPJ with spaces WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val cnpjWithSpaces = "12.345.678/0001 90" // contém espaço

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(cnpjWithSpaces)
        }
    }

    @Test
    fun `GIVEN CNPJ with wrong separator positions WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val wrongSeparators = "123.45678/0001-90" // separadores em posições erradas

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(wrongSeparators)
        }
    }

    @Test
    fun `GIVEN CNPJ with mask but wrong number of digits WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val wrongDigits = "12.345.678/000-90" // número incorreto de dígitos na máscara

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(wrongDigits)
        }
    }

    @Test
    fun `GIVEN string with only special characters WHEN creating CNPJ THEN should throw IllegalArgumentException`() {
        // GIVEN
        val onlySpecialChars = "..//--"

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ(onlySpecialChars)
        }
    }
}

