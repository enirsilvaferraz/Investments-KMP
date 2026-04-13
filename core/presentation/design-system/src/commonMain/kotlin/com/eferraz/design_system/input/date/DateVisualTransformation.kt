package com.eferraz.design_system.input.date

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private const val MAX_DATE_DIGITS: Int = 8

private const val YEAR_MM_DD_AFTER_YEAR_DIGIT_INDEX: Int = 3
private const val YEAR_MM_DD_AFTER_MONTH_DIGIT_INDEX: Int = 5

private const val DD_MM_YYYY_AFTER_DAY_DIGIT_INDEX: Int = 1
private const val DD_MM_YYYY_AFTER_MONTH_DIGIT_INDEX: Int = 3

/** Último índice (0-based) do dígito de cada grupo após o qual se insere o separador (AAAA-MM-DD). */
private val YEAR_MONTH_DAY_SEPARATOR_AFTER_DIGIT_INDEX: List<Int> = listOf(
    YEAR_MM_DD_AFTER_YEAR_DIGIT_INDEX,
    YEAR_MM_DD_AFTER_MONTH_DIGIT_INDEX,
)

/** Último índice do dígito de cada grupo após o qual se insere o separador (DD/MM/AAAA). */
private val DAY_MONTH_YEAR_SEPARATOR_AFTER_DIGIT_INDEX: List<Int> = listOf(
    DD_MM_YYYY_AFTER_DAY_DIGIT_INDEX,
    DD_MM_YYYY_AFTER_MONTH_DIGIT_INDEX,
)

/**
 * Texto lógico do campo com [DateVisualTransformation]: só dígitos, no máximo oito (DDMMAAAA ou AAAAMMDD).
 * Usar em `onValueChange` para o estado não herdar lixo (colar, IME, etc.); a transformação só afeta a vista.
 */
public fun filterDateMaskDigits(input: String): String =
    input.filter { it.isDigit() }.take(MAX_DATE_DIGITS)

/** Formato de máscara para [DateVisualTransformation]. */
public enum class DateFormat {
    /** AAAA-MM-DD */
    YYYY_MM_DD,

    /** DD/MM/AAAA */
    DD_MM_YYYY,
}

/**
 * [VisualTransformation] que exibe até 8 dígitos como data com separadores;
 * o texto lógico do campo deve ser só dígitos (ex.: `20240115`).
 */
public class DateVisualTransformation(
    private val format: DateFormat,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = filterDateMaskDigits(text.text)

        val (separator, insertAfterIndices) = when (format) {
            DateFormat.YYYY_MM_DD -> "-" to YEAR_MONTH_DAY_SEPARATOR_AFTER_DIGIT_INDEX
            DateFormat.DD_MM_YYYY -> "/" to DAY_MONTH_YEAR_SEPARATOR_AFTER_DIGIT_INDEX
        }

        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index in insertAfterIndices && index < digits.length - 1) {
                    append(separator)
                }
            }
        }

        val offsetMapping = createOffsetMapping(
            insertAfterIndices = insertAfterIndices,
            separatorLength = separator.length,
            originalLength = digits.length,
            transformedLength = formatted.length,
        )

        return TransformedText(text = AnnotatedString(text = formatted), offsetMapping = offsetMapping)
    }

    private fun createOffsetMapping(
        insertAfterIndices: List<Int>,
        separatorLength: Int,
        originalLength: Int,
        transformedLength: Int,
    ): OffsetMapping {
        val separatorPositions = insertAfterIndices.mapIndexed { i, index ->
            index + 1 + (i * separatorLength)
        }

        return object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var result = offset
                insertAfterIndices.forEach { index ->
                    if (offset >= index + 1) result += separatorLength
                }
                return result.coerceIn(0, transformedLength)
            }

            override fun transformedToOriginal(offset: Int): Int =
                mapTransformedOffsetToOriginal(
                    offset = offset,
                    separatorPositions = separatorPositions,
                    separatorLength = separatorLength,
                    insertAfterIndices = insertAfterIndices,
                    originalLength = originalLength,
                )
        }
    }
}

private fun mapTransformedOffsetToOriginal(
    offset: Int,
    separatorPositions: List<Int>,
    separatorLength: Int,
    insertAfterIndices: List<Int>,
    originalLength: Int,
): Int =
    when {
        offset <= 0 -> 0
        else -> {
            val hitIndex = separatorPositions.indices.firstOrNull { i ->
                val sepPos = separatorPositions[i]
                offset > sepPos && offset <= sepPos + separatorLength
            }
            if (hitIndex != null) {
                (insertAfterIndices[hitIndex] + 1).coerceIn(0, originalLength)
            } else {
                val separatorsBefore = separatorPositions.count { offset > it + separatorLength }
                (offset - separatorsBefore * separatorLength).coerceIn(0, originalLength)
            }
        }
    }
