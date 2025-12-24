package com.eferraz.presentation.design_system.components

import androidx.compose.ui.text.AnnotatedString
import com.eferraz.presentation.design_system.components.inputs.DateFormat
import com.eferraz.presentation.design_system.components.inputs.DateVisualTransformation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateVisualTransformationTest {

    @Test
    fun `GIVEN empty text WHEN filter YYYY_MM_DD THEN returns empty formatted text`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("", result.text.text)
    }

    @Test
    fun `GIVEN empty text WHEN filter DD_MM_YYYY THEN returns empty formatted text`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("", result.text.text)
    }

    @Test
    fun `GIVEN 8 digits WHEN filter YYYY_MM_DD THEN formats correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("2024-01-15", result.text.text)
    }

    @Test
    fun `GIVEN 8 digits WHEN filter DD_MM_YYYY THEN formats correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("15/01/2024", result.text.text)
    }

    @Test
    fun `GIVEN less than 8 digits WHEN filter YYYY_MM_DD THEN formats partially`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("2024", result.text.text)
    }

    @Test
    fun `GIVEN less than 8 digits WHEN filter DD_MM_YYYY THEN formats partially`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("15", result.text.text)
    }

    @Test
    fun `GIVEN more than 8 digits WHEN filter YYYY_MM_DD THEN formats only first 8 digits`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115123")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("2024-01-15", result.text.text)
    }

    @Test
    fun `GIVEN more than 8 digits WHEN filter DD_MM_YYYY THEN formats only first 8 digits`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024123")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("15/01/2024", result.text.text)
    }

    @Test
    fun `GIVEN text with non-digit characters WHEN filter YYYY_MM_DD THEN filters and formats only digits`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024abc0115xyz")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("2024-01-15", result.text.text)
    }

    @Test
    fun `GIVEN text with non-digit characters WHEN filter DD_MM_YYYY THEN filters and formats only digits`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15abc01xyz2024")

        // Act
        val result = transformation.filter(input)

        // Assert
        assertEquals("15/01/2024", result.text.text)
    }

    @Test
    fun `GIVEN partial date YYYY_MM_DD WHEN filter THEN formats with separators at correct positions`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)

        // Act & Assert
        assertEquals("2", transformation.filter(AnnotatedString("2")).text.text)
        assertEquals("20", transformation.filter(AnnotatedString("20")).text.text)
        assertEquals("202", transformation.filter(AnnotatedString("202")).text.text)
        assertEquals("2024", transformation.filter(AnnotatedString("2024")).text.text)
        assertEquals("2024-0", transformation.filter(AnnotatedString("20240")).text.text)
        assertEquals("2024-01", transformation.filter(AnnotatedString("202401")).text.text)
        assertEquals("2024-01-1", transformation.filter(AnnotatedString("2024011")).text.text)
        assertEquals("2024-01-15", transformation.filter(AnnotatedString("20240115")).text.text)
    }

    @Test
    fun `GIVEN partial date DD_MM_YYYY WHEN filter THEN formats with separators at correct positions`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)

        // Act & Assert
        assertEquals("1", transformation.filter(AnnotatedString("1")).text.text)
        assertEquals("15", transformation.filter(AnnotatedString("15")).text.text)
        assertEquals("15/0", transformation.filter(AnnotatedString("150")).text.text)
        assertEquals("15/01", transformation.filter(AnnotatedString("1501")).text.text)
        assertEquals("15/01/2", transformation.filter(AnnotatedString("15012")).text.text) // Após índice 3 (4º dígito)
        assertEquals("15/01/20", transformation.filter(AnnotatedString("150120")).text.text)
        assertEquals("15/01/202", transformation.filter(AnnotatedString("1501202")).text.text)
        assertEquals("15/01/2024", transformation.filter(AnnotatedString("15012024")).text.text)
    }

    @Test
    fun `GIVEN YYYY_MM_DD WHEN originalToTransformed THEN maps offsets correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        // Original: 0 1 2 3 4 5 6 7 (offsets 0-8)
        // Transformado: 0 1 2 3 - 4 5 - 6 7 (offsets 0-10)
        // Separadores após índices 3 e 5
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
        assertEquals(3, offsetMapping.originalToTransformed(3))
        assertEquals(5, offsetMapping.originalToTransformed(4)) // Após 4º dígito, pula "-"
        assertEquals(6, offsetMapping.originalToTransformed(5))
        assertEquals(8, offsetMapping.originalToTransformed(6)) // Após 6º dígito, pula "-"
        assertEquals(9, offsetMapping.originalToTransformed(7))
        assertEquals(10, offsetMapping.originalToTransformed(8)) // Após último dígito
    }

    @Test
    fun `GIVEN DD_MM_YYYY WHEN originalToTransformed THEN maps offsets correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        // Original: 0 1 2 3 4 5 6 7 (offsets 0-8)
        // Transformado: 0 1 / 2 3 / 4 5 6 7 (offsets 0-10)
        // Separadores após índices 1 e 3
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(3, offsetMapping.originalToTransformed(2)) // Após 2º dígito, pula "/"
        assertEquals(4, offsetMapping.originalToTransformed(3))
        assertEquals(6, offsetMapping.originalToTransformed(4)) // Após 4º dígito, pula "/"
        assertEquals(7, offsetMapping.originalToTransformed(5))
        assertEquals(8, offsetMapping.originalToTransformed(6))
        assertEquals(9, offsetMapping.originalToTransformed(7))
        assertEquals(10, offsetMapping.originalToTransformed(8)) // Após último dígito
    }

    @Test
    fun `GIVEN YYYY_MM_DD WHEN transformedToOriginal THEN maps offsets correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        // Transformado: 0 1 2 3 - 4 5 - 6 7 (offsets 0-10)
        // Original: 0 1 2 3 4 5 6 7 (offsets 0-8)
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(1, offsetMapping.transformedToOriginal(1))
        assertEquals(2, offsetMapping.transformedToOriginal(2))
        assertEquals(3, offsetMapping.transformedToOriginal(3))
        assertEquals(4, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5)) // "-" mapeia para 4
        assertEquals(5, offsetMapping.transformedToOriginal(6))
        assertEquals(6, offsetMapping.transformedToOriginal(7))
        assertEquals(6, offsetMapping.transformedToOriginal(8)) // "-" mapeia para 6
        assertEquals(7, offsetMapping.transformedToOriginal(9))
        assertEquals(8, offsetMapping.transformedToOriginal(10)) // Após último caractere
    }

    @Test
    fun `GIVEN DD_MM_YYYY WHEN transformedToOriginal THEN maps offsets correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        // Transformado: 0 1 / 2 3 / 4 5 6 7 (offsets 0-10)
        // Original: 0 1 2 3 4 5 6 7 (offsets 0-8)
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(1, offsetMapping.transformedToOriginal(1))
        assertEquals(2, offsetMapping.transformedToOriginal(2))
        assertEquals(2, offsetMapping.transformedToOriginal(3)) // "/" mapeia para 2
        assertEquals(3, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5))
        assertEquals(4, offsetMapping.transformedToOriginal(6)) // "/" mapeia para 4
        assertEquals(5, offsetMapping.transformedToOriginal(7))
        assertEquals(6, offsetMapping.transformedToOriginal(8))
        assertEquals(7, offsetMapping.transformedToOriginal(9))
        assertEquals(8, offsetMapping.transformedToOriginal(10)) // Após último caractere
    }

    @Test
    fun `GIVEN YYYY_MM_DD WHEN offset mapping round trip THEN returns original offset`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert - Testa ida e volta para todos os offsets válidos
        for (originalOffset in 0..8) {
            val transformed = offsetMapping.originalToTransformed(originalOffset)
            val backToOriginal = offsetMapping.transformedToOriginal(transformed)
            assertEquals(originalOffset, backToOriginal, 
                "Round trip failed for original offset $originalOffset")
        }
    }

    @Test
    fun `GIVEN DD_MM_YYYY WHEN offset mapping round trip THEN returns original offset`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15012024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert - Testa ida e volta para todos os offsets válidos
        for (originalOffset in 0..8) {
            val transformed = offsetMapping.originalToTransformed(originalOffset)
            val backToOriginal = offsetMapping.transformedToOriginal(transformed)
            assertEquals(originalOffset, backToOriginal, 
                "Round trip failed for original offset $originalOffset")
        }
    }

    @Test
    fun `GIVEN partial input YYYY_MM_DD WHEN offset mapping THEN handles correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("2024")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
        assertEquals(3, offsetMapping.originalToTransformed(3))
        assertEquals(4, offsetMapping.originalToTransformed(4))
    }

    @Test
    fun `GIVEN partial input DD_MM_YYYY WHEN offset mapping THEN handles correctly`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.DD_MM_YYYY)
        val input = AnnotatedString("15")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(2, offsetMapping.originalToTransformed(2))
    }

    @Test
    fun `GIVEN offset out of bounds WHEN originalToTransformed THEN clamps to valid range`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        val maxTransformed = result.text.length
        assertTrue(offsetMapping.originalToTransformed(-1) >= 0)
        assertTrue(offsetMapping.originalToTransformed(100) <= maxTransformed)
    }

    @Test
    fun `GIVEN offset out of bounds WHEN transformedToOriginal THEN clamps to valid range`() {
        // Arrange
        val transformation = DateVisualTransformation(DateFormat.YYYY_MM_DD)
        val input = AnnotatedString("20240115")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Act & Assert
        val maxOriginal = input.length
        assertTrue(offsetMapping.transformedToOriginal(-1) >= 0)
        assertTrue(offsetMapping.transformedToOriginal(100) <= maxOriginal)
    }
}

