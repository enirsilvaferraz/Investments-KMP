package com.eferraz.design_system.input.date

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [DateVisualTransformation] formatting and offset mapping.
 */
internal class DateVisualTransformationTest {

    /**
     * Empty input yields empty display text for YYYY-MM-DD.
     */
    @Test
    fun `GIVEN empty text WHEN filter YYYY_MM_DD THEN returns empty formatted text`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("", result.text.text)
    }

    /**
     * Empty input yields empty display text for DD/MM/YYYY.
     */
    @Test
    fun `GIVEN empty text WHEN filter DD_MM_YYYY THEN returns empty formatted text`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("", result.text.text)
    }

    /**
     * Full 8-digit input formats to ISO-style date with hyphens.
     */
    @Test
    fun `GIVEN 8 digits WHEN filter YYYY_MM_DD THEN formats correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("2024-01-15", result.text.text)
    }

    /**
     * Full 8-digit input formats to day-first date with slashes.
     */
    @Test
    fun `GIVEN 8 digits WHEN filter DD_MM_YYYY THEN formats correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("15/01/2024", result.text.text)
    }

    /**
     * Partial input for YYYY-MM-DD shows digits only until complete.
     */
    @Test
    fun `GIVEN less than 8 digits WHEN filter YYYY_MM_DD THEN formats partially`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("2024", result.text.text)
    }

    /**
     * Partial input for DD/MM/YYYY shows digits only until complete.
     */
    @Test
    fun `GIVEN less than 8 digits WHEN filter DD_MM_YYYY THEN formats partially`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("15", result.text.text)
    }

    /**
     * Extra digits after the eighth are ignored for YYYY-MM-DD.
     */
    @Test
    fun `GIVEN more than 8 digits WHEN filter YYYY_MM_DD THEN formats only first 8 digits`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115123")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("2024-01-15", result.text.text)
    }

    /**
     * Extra digits after the eighth are ignored for DD/MM/YYYY.
     */
    @Test
    fun `GIVEN more than 8 digits WHEN filter DD_MM_YYYY THEN formats only first 8 digits`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024123")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("15/01/2024", result.text.text)
    }

    /**
     * Non-digit characters are stripped before formatting (YYYY-MM-DD).
     */
    @Test
    fun `GIVEN text with non-digit characters WHEN filter YYYY_MM_DD THEN filters and formats only digits`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024abc0115xyz")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("2024-01-15", result.text.text)
    }

    /**
     * Non-digit characters are stripped before formatting (DD/MM/YYYY).
     */
    @Test
    fun `GIVEN text with non-digit characters WHEN filter DD_MM_YYYY THEN filters and formats only digits`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15abc01xyz2024")

        // WHEN
        val result = transformation.filter(input)

        // THEN
        assertEquals("15/01/2024", result.text.text)
    }

    /**
     * Incremental typing builds separators at the correct indices for YYYY-MM-DD.
     */
    @Test
    fun `GIVEN partial date YYYY_MM_DD WHEN filter THEN formats with separators at correct positions`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)

        // WHEN
        val f2 = transformation.filter(AnnotatedString("2")).text.text
        val f20 = transformation.filter(AnnotatedString("20")).text.text
        val f202 = transformation.filter(AnnotatedString("202")).text.text
        val f2024 = transformation.filter(AnnotatedString("2024")).text.text
        val f20240 = transformation.filter(AnnotatedString("20240")).text.text
        val f202401 = transformation.filter(AnnotatedString("202401")).text.text
        val f2024011 = transformation.filter(AnnotatedString("2024011")).text.text
        val f20240115 = transformation.filter(AnnotatedString("20240115")).text.text

        // THEN
        assertEquals("2", f2)
        assertEquals("20", f20)
        assertEquals("202", f202)
        assertEquals("2024", f2024)
        assertEquals("2024-0", f20240)
        assertEquals("2024-01", f202401)
        assertEquals("2024-01-1", f2024011)
        assertEquals("2024-01-15", f20240115)
    }

    /**
     * Incremental typing builds separators at the correct indices for DD/MM/YYYY.
     */
    @Test
    fun `GIVEN partial date DD_MM_YYYY WHEN filter THEN formats with separators at correct positions`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)

        // WHEN
        val f1 = transformation.filter(AnnotatedString("1")).text.text
        val f15 = transformation.filter(AnnotatedString("15")).text.text
        val f150 = transformation.filter(AnnotatedString("150")).text.text
        val f1501 = transformation.filter(AnnotatedString("1501")).text.text
        val f15012 = transformation.filter(AnnotatedString("15012")).text.text
        val f150120 = transformation.filter(AnnotatedString("150120")).text.text
        val f1501202 = transformation.filter(AnnotatedString("1501202")).text.text
        val f15012024 = transformation.filter(AnnotatedString("15012024")).text.text

        // THEN
        assertEquals("1", f1)
        assertEquals("15", f15)
        assertEquals("15/0", f150)
        assertEquals("15/01", f1501)
        assertEquals("15/01/2", f15012)
        assertEquals("15/01/20", f150120)
        assertEquals("15/01/202", f1501202)
        assertEquals("15/01/2024", f15012024)
    }

    /**
     * originalToTransformed maps raw digit indices to displayed positions (YYYY-MM-DD).
     */
    @Test
    fun `GIVEN YYYY_MM_DD WHEN originalToTransformed THEN maps offsets correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
        assertEquals(3, offsetMapping.originalToTransformed(3))
        assertEquals(5, offsetMapping.originalToTransformed(4))
        assertEquals(6, offsetMapping.originalToTransformed(5))
        assertEquals(8, offsetMapping.originalToTransformed(6))
        assertEquals(9, offsetMapping.originalToTransformed(7))
        assertEquals(10, offsetMapping.originalToTransformed(8))
    }

    /**
     * originalToTransformed maps raw digit indices to displayed positions (DD/MM/YYYY).
     */
    @Test
    fun `GIVEN DD_MM_YYYY WHEN originalToTransformed THEN maps offsets correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(3, offsetMapping.originalToTransformed(2))
        assertEquals(4, offsetMapping.originalToTransformed(3))
        assertEquals(6, offsetMapping.originalToTransformed(4))
        assertEquals(7, offsetMapping.originalToTransformed(5))
        assertEquals(8, offsetMapping.originalToTransformed(6))
        assertEquals(9, offsetMapping.originalToTransformed(7))
        assertEquals(10, offsetMapping.originalToTransformed(8))
    }

    /**
     * transformedToOriginal maps displayed caret positions back to raw indices (YYYY-MM-DD).
     */
    @Test
    fun `GIVEN YYYY_MM_DD WHEN transformedToOriginal THEN maps offsets correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(1, offsetMapping.transformedToOriginal(1))
        assertEquals(2, offsetMapping.transformedToOriginal(2))
        assertEquals(3, offsetMapping.transformedToOriginal(3))
        assertEquals(4, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5))
        assertEquals(5, offsetMapping.transformedToOriginal(6))
        assertEquals(6, offsetMapping.transformedToOriginal(7))
        assertEquals(6, offsetMapping.transformedToOriginal(8))
        assertEquals(7, offsetMapping.transformedToOriginal(9))
        assertEquals(8, offsetMapping.transformedToOriginal(10))
    }

    /**
     * transformedToOriginal maps displayed caret positions back to raw indices (DD/MM/YYYY).
     */
    @Test
    fun `GIVEN DD_MM_YYYY WHEN transformedToOriginal THEN maps offsets correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(1, offsetMapping.transformedToOriginal(1))
        assertEquals(2, offsetMapping.transformedToOriginal(2))
        assertEquals(2, offsetMapping.transformedToOriginal(3))
        assertEquals(3, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5))
        assertEquals(4, offsetMapping.transformedToOriginal(6))
        assertEquals(5, offsetMapping.transformedToOriginal(7))
        assertEquals(6, offsetMapping.transformedToOriginal(8))
        assertEquals(7, offsetMapping.transformedToOriginal(9))
        assertEquals(8, offsetMapping.transformedToOriginal(10))
    }

    /**
     * Round-trip: originalToTransformed then transformedToOriginal restores each valid offset (YYYY-MM-DD).
     */
    @Test
    fun `GIVEN YYYY_MM_DD WHEN offset mapping round trip THEN returns original offset`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN
        val roundTrips = (0..8).map { originalOffset ->
            val transformed = offsetMapping.originalToTransformed(originalOffset)
            offsetMapping.transformedToOriginal(transformed)
        }

        // THEN
        roundTrips.forEachIndexed { originalOffset, backToOriginal ->
            assertEquals(
                originalOffset,
                backToOriginal,
                "Round trip failed for original offset $originalOffset",
            )
        }
    }

    /**
     * Round-trip: originalToTransformed then transformedToOriginal restores each valid offset (DD/MM/YYYY).
     */
    @Test
    fun `GIVEN DD_MM_YYYY WHEN offset mapping round trip THEN returns original offset`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN
        val roundTrips = (0..8).map { originalOffset ->
            val transformed = offsetMapping.originalToTransformed(originalOffset)
            offsetMapping.transformedToOriginal(transformed)
        }

        // THEN
        roundTrips.forEachIndexed { originalOffset, backToOriginal ->
            assertEquals(
                originalOffset,
                backToOriginal,
                "Round trip failed for original offset $originalOffset",
            )
        }
    }

    /**
     * Partial YYYY-MM-DD input produces consistent offset mapping for typed prefix.
     */
    @Test
    fun `GIVEN partial input YYYY_MM_DD WHEN offset mapping THEN handles correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
        assertEquals(3, offsetMapping.originalToTransformed(3))
        assertEquals(4, offsetMapping.originalToTransformed(4))
    }

    /**
     * Partial DD/MM/YYYY input produces consistent offset mapping for typed prefix.
     */
    @Test
    fun `GIVEN partial input DD_MM_YYYY WHEN offset mapping THEN handles correctly`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN & THEN
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
    }

    /**
     * Out-of-range original offsets are clamped for YYYY-MM-DD.
     */
    @Test
    fun `GIVEN offset out of bounds WHEN originalToTransformed THEN clamps to valid range`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN
        val maxTransformed = result.text.length
        val low = offsetMapping.originalToTransformed(-1)
        val high = offsetMapping.originalToTransformed(100)

        // THEN
        assertTrue(low >= 0)
        assertTrue(high <= maxTransformed)
    }

    /**
     * Out-of-range transformed offsets are clamped for YYYY-MM-DD.
     */
    @Test
    fun `GIVEN offset out of bounds WHEN transformedToOriginal THEN clamps to valid range`() {

        // GIVEN
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // WHEN
        val maxOriginal = input.length
        val low = offsetMapping.transformedToOriginal(-1)
        val high = offsetMapping.transformedToOriginal(100)

        // THEN
        assertTrue(low >= 0)
        assertTrue(high <= maxOriginal)
    }
}
