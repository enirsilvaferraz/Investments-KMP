package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Enum para definir o formato de data
 */
internal enum class DateFormat {
    YYYY_MM_DD,  // YYYY-MM-DD
    DD_MM_YYYY   // dd/MM/YYYY
}

/**
 * VisualTransformation para formatar datas conforme o padrão especificado
 */
internal class DateVisualTransformation(
    private val format: DateFormat,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val digits = text.text.filter { it.isDigit() }.take(8)

        val (separator, insertAfterIndices) = when (format) {
            DateFormat.YYYY_MM_DD -> "-" to listOf(3, 5) // Após índices 3 e 5 (YYYY-MM-DD: após 4º e 6º dígitos)
            DateFormat.DD_MM_YYYY -> "/" to listOf(1, 3) // Após índices 1 e 3 (DD/MM/YYYY: após 2º e 4º dígitos)
        }

        val formatted = buildString {

            digits.forEachIndexed { index, char ->

                append(char)

                // Só insere separador se houver mais dígitos após este índice
                if (index in insertAfterIndices && index < digits.length - 1) {
                    append(separator)
                }
            }
        }

        val offsetMapping = createOffsetMapping(insertAfterIndices, separator.length, digits.length, formatted.length)

        return TransformedText(text = AnnotatedString(text = formatted), offsetMapping = offsetMapping)
    }

    private fun createOffsetMapping(
        insertAfterIndices: List<Int>,
        separatorLength: Int,
        originalLength: Int,
        transformedLength: Int,
    ): OffsetMapping {

        // Calcula onde os separadores aparecem no texto transformado
        val separatorPositions = insertAfterIndices.mapIndexed { i, index ->
            index + 1 + (i * separatorLength)
        }

        return object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {

                if (offset <= 0) return 0
                var result = offset

                insertAfterIndices.forEach { index ->
                    // Se o offset está após ou no índice especificado, adiciona o separador
                    // offset 4 significa "após o 4º dígito" (índice 3), então offset >= 3 + 1 = 4 >= 4 é true
                    if (offset >= index + 1) result += separatorLength
                }

                return result.coerceIn(0, transformedLength)
            }

            override fun transformedToOriginal(offset: Int): Int {

                if (offset <= 0) return 0

                // Verifica se o offset está em um separador
                separatorPositions.forEachIndexed { i, sepPos ->
                    if (offset > sepPos && offset <= sepPos + separatorLength) {
                        return (insertAfterIndices[i] + 1).coerceIn(0, originalLength)
                    }
                }

                // Conta quantos separadores estão antes do offset
                val separatorsBefore = separatorPositions.count { offset > it + separatorLength }
                return (offset - separatorsBefore * separatorLength).coerceIn(0, originalLength)
            }
        }
    }
}

@Composable
internal fun TableInputDate(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    enabled: Boolean = true,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }
    val isHoveredState by actualInteractionSource.collectIsHoveredAsState()
    val isFocusedState by actualInteractionSource.collectIsFocusedAsState()

    TableInputDate(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        dateFormat = dateFormat,
        enabled = enabled,
        actualInteractionSource = actualInteractionSource,
        isHovered = isHoveredState,
        isFocused = isFocusedState
    )
}

@Composable
private fun TableInputDate(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    enabled: Boolean = true,
    actualInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean = false,
    isFocused: Boolean = false,
) {

    // Mantém o estado interno do texto
    var textFieldValueState: TextFieldValue by remember(value) {
        val digits = value.filter { it.isDigit() }
        mutableStateOf(TextFieldValue(text = digits, selection = TextRange(digits.length)))
    }

    // Sincroniza quando o valor externo muda
    LaunchedEffect(value) {

        val currentDigits = textFieldValueState.text.filter { it.isDigit() }
        val externalDigits = value.filter { it.isDigit() }

        if (currentDigits != externalDigits) {
            textFieldValueState = TextFieldValue(text = externalDigits, selection = TextRange(externalDigits.length))
        }
    }

    val colors = MaterialTheme.colorScheme
    val textColor = if (enabled) colors.onSurface else colors.onSurfaceVariant

    TableInputLookAndFeel(
        modifier = modifier,
        actualInteractionSource = actualInteractionSource,
        enabled = enabled,
        isHovered = isHovered,
        isFocused = isFocused
    ) {

        BasicTextField(
            value = textFieldValueState,
            enabled = enabled,
            onValueChange = { newValue ->

                // Filtra apenas dígitos
                val filteredText = newValue.text.filter { it.isDigit() }

                // Limita a 8 dígitos (DDMMYYYY ou YYYYMMDD)
                val limitedText = if (filteredText.length > 8) {
                    filteredText.substring(0..7)
                } else {
                    filteredText
                }

                // Mantém a posição do cursor
                val cursorOffset = if (limitedText.length < newValue.text.length) {
                    minOf(newValue.selection.start.coerceIn(0, limitedText.length), limitedText.length)
                } else {
                    val removedBeforeCursor = newValue.text.take(newValue.selection.start).count { !it.isDigit() }
                    (newValue.selection.start - removedBeforeCursor).coerceIn(0, limitedText.length)
                }

                textFieldValueState = TextFieldValue(
                    text = limitedText,
                    selection = TextRange(cursorOffset)
                )

                // Atualiza o valor externo apenas com dígitos
                onValueChange(limitedText)
            },
            modifier = Modifier.padding(horizontal = 8.dp).width(85.dp),
            interactionSource = actualInteractionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            visualTransformation = DateVisualTransformation(dateFormat)
        )
    }
}

@Preview
@Composable
internal fun TableInputDatePreview() {

    Surface {

        // Estado Normal (sem hover, sem focus)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Normal", style = MaterialTheme.typography.labelSmall)
            var value1 by remember { mutableStateOf("20240115") }
            TableInputDate(
                value = value1,
                onValueChange = { value1 = it },
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputDateHoverPreview() {

    Surface {

        // Estado Hover (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Hover", style = MaterialTheme.typography.labelSmall)
            var value2 by remember { mutableStateOf("20240115") }
            TableInputDate(
                value = value2,
                onValueChange = { value2 = it },
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = true,
                isHovered = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputDateFocusedPreview() {

    Surface {

        // Estado Focused (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Focused", style = MaterialTheme.typography.labelSmall)
            var value3 by remember { mutableStateOf("20240115") }
            TableInputDate(
                value = value3,
                onValueChange = { value3 = it },
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = true,
                isFocused = true
            )
        }
    }
}


@Preview
@Composable
internal fun TableInputDateDisabledPreview() {

    Surface {

        // Estado Disabled
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Disabled", style = MaterialTheme.typography.labelSmall)
            TableInputDate(
                value = "20240115",
                onValueChange = {},
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = false
            )
        }
    }
}


@Preview
@Composable
internal fun TableInputDateDDMMYYYPreview() {

    Surface {

        // Formato DD/MM/YYYY
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("DD/MM/YYYY", style = MaterialTheme.typography.labelSmall)
            var value4 by remember { mutableStateOf("15012024") }
            TableInputDate(
                value = value4,
                onValueChange = { value4 = it },
                dateFormat = DateFormat.DD_MM_YYYY,
                enabled = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputDateEmptyPreview() {

    Surface {

        // Estado vazio
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Vazio", style = MaterialTheme.typography.labelSmall)
            var value5 by remember { mutableStateOf("") }
            TableInputDate(
                value = value5,
                onValueChange = { value5 = it },
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = true
            )
        }
    }
}