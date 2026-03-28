package com.eferraz.entities.value

import com.eferraz.entities.assets.CNPJ
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CNPJTest {

    /**
     * Valid masked CNPJ string is accepted and returned unchanged.
     */
    @Test
    fun `GIVEN valid masked CNPJ string THEN instance holds same value`() {

        // GIVEN
        val input = "12.345.678/0001-90"

        // WHEN
        val cnpj = CNPJ(input)

        // THEN
        assertEquals("12.345.678/0001-90", cnpj.get())
    }

    /**
     * Valid unmasked 14-digit CNPJ is accepted.
     */
    @Test
    fun `GIVEN valid unmasked CNPJ string THEN instance holds same digits`() {

        // GIVEN
        val input = "12345678000190"

        // WHEN
        val cnpj = CNPJ(input)

        // THEN
        assertEquals("12345678000190", cnpj.get())
    }

    /**
     * Another valid masked format with different digits is accepted.
     */
    @Test
    fun `GIVEN valid masked CNPJ with different digits THEN instance holds same value`() {

        // GIVEN
        val input = "11.222.333/4444-55"

        // WHEN
        val cnpj = CNPJ(input)

        // THEN
        assertEquals("11.222.333/4444-55", cnpj.get())
    }

    /**
     * Empty string is rejected.
     */
    @Test
    fun `GIVEN empty string THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("")
        }
    }

    /**
     * Fewer than 14 digits is rejected.
     */
    @Test
    fun `GIVEN CNPJ with fewer than 14 digits THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("1234567890123") // 13 digits
        }
    }

    /**
     * More than 14 digits is rejected.
     */
    @Test
    fun `GIVEN CNPJ with more than 14 digits THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("123456789012345") // 15 digits
        }
    }

    /**
     * Wrong separator pattern is rejected.
     */
    @Test
    fun `GIVEN incorrect mask pattern THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("12-345-678/0001.90")
        }
    }

    /**
     * Incomplete mask is rejected.
     */
    @Test
    fun `GIVEN incomplete mask THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001")
        }
    }

    /**
     * Non-numeric characters in digit positions are rejected.
     */
    @Test
    fun `GIVEN CNPJ with non-numeric digit positions THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001-AB")
        }
    }

    /**
     * Spaces in the string are rejected.
     */
    @Test
    fun `GIVEN CNPJ containing spaces THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001 90")
        }
    }

    /**
     * Separators in wrong positions are rejected.
     */
    @Test
    fun `GIVEN separators in wrong positions THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("123.45678/0001-90")
        }
    }

    /**
     * Mask with wrong digit count in a segment is rejected.
     */
    @Test
    fun `GIVEN mask with wrong segment digit count THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/000-90")
        }
    }

    /**
     * String with only special characters is rejected.
     */
    @Test
    fun `GIVEN string with only punctuation THEN throws IllegalArgumentException`() {

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            CNPJ("..//--")
        }
    }
}
