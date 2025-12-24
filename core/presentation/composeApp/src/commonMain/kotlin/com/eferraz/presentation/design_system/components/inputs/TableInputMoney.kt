package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.helpers.CurrencyVisualTransformation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun TableInputMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }
    val isHoveredState by actualInteractionSource.collectIsHoveredAsState()
    val isFocusedState by actualInteractionSource.collectIsFocusedAsState()

    TableInputMoney(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        actualInteractionSource = actualInteractionSource,
        isHovered = isHoveredState,
        isFocused = isFocusedState
    )
}

@Composable
private fun TableInputMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actualInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean = false,
    isFocused: Boolean = false,
) {

    // Mantém o estado interno persistente, não recria quando value muda
    var textFieldValueState: TextFieldValue by remember {
        val textValue = (value * 100).toInt().toString()
        mutableStateOf(
            TextFieldValue(
                text = textValue,
                selection = TextRange(textValue.length)
            )
        )
    }

    // Sincroniza o estado interno com o valor externo apenas quando o valor externo muda
    // de forma independente (não causado pela edição do usuário)
    LaunchedEffect(value) {

        val currentTextValue = textFieldValueState.text.trimStart { it == '0' }
        val currentValue = if (currentTextValue.isEmpty()) 0.0 else currentTextValue.toDouble() / 100

        // Só atualiza se o valor externo for diferente do valor atual do campo
        // Isso evita resetar o campo quando o usuário está editando
        if (kotlin.math.abs(currentValue - value) > 0.001) {
            val textValue = (value * 100).toInt().toString()
            textFieldValueState = TextFieldValue(
                text = textValue,
                selection = TextRange(textValue.length)
            )
        }
    }

    val colors = MaterialTheme.colorScheme
    val textColor = if (enabled) colors.onSurface else colors.onSurfaceVariant

    fun onValueChangeInternal(newValue: TextFieldValue) {

        // Filtra apenas dígitos (0-9)
        val filteredText = newValue.text.filter { char -> char.isDigit() }

        // Mantém a posição do cursor ajustada após a filtragem
        val cursorOffset = if (filteredText.length < newValue.text.length) {
            // Se caracteres foram removidos, ajusta o cursor
            minOf(newValue.selection.start.coerceIn(0, filteredText.length), filteredText.length)
        } else {
            // Se não houve remoção, mantém a posição relativa
            val removedBeforeCursor = newValue.text.take(newValue.selection.start).count { !it.isDigit() }
            (newValue.selection.start - removedBeforeCursor).coerceIn(0, filteredText.length)
        }

        val newTextFieldValue = TextFieldValue(
            text = filteredText,
            selection = TextRange(cursorOffset)
        )

        textFieldValueState = newTextFieldValue

        val string = filteredText.trimStart { it == '0' }

        if (string.isEmpty()) onValueChange(null)
        else onValueChange(string.toDouble() / 100)
    }

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
            onValueChange = { newValue -> onValueChangeInternal(newValue) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            interactionSource = actualInteractionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.End,
                color = textColor
            ),
            visualTransformation = CurrencyVisualTransformation
        )
    }
}

@Preview
@Composable
internal fun TableInputMoneyPreview() {

    Surface {

        // Estado Normal (sem hover, sem focus)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Normal", style = MaterialTheme.typography.labelSmall)
            var value1 by remember { mutableStateOf(1234.56) }
            TableInputMoney(
                value = value1,
                onValueChange = { value1 = it ?: 0.0 },
                enabled = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputMoneyHoverPreview() {

    Surface {

        // Estado Hover (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Hover", style = MaterialTheme.typography.labelSmall)
            var value2 by remember { mutableStateOf(1234.56) }
            TableInputMoney(
                value = value2,
                onValueChange = { value2 = it ?: 0.0 },
                enabled = true,
                isHovered = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputMoneyFocusedPreview() {

    Surface {

        // Estado Focused (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Focused", style = MaterialTheme.typography.labelSmall)
            var value3 by remember { mutableStateOf(1234.56) }
            TableInputMoney(
                value = value3,
                onValueChange = { value3 = it ?: 0.0 },
                enabled = true,
                isFocused = true
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputMoneyDisabledPreview() {

    Surface {

        // Estado Disabled
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Disabled", style = MaterialTheme.typography.labelSmall)
            TableInputMoney(
                value = 1234.56,
                onValueChange = {},
                enabled = false
            )
        }
    }
}

@Preview
@Composable
internal fun TableInputMoneyEmptyPreview() {

    Surface {

        // Estado vazio
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Vazio", style = MaterialTheme.typography.labelSmall)
            var value5 by remember { mutableStateOf(0.0) }
            TableInputMoney(
                value = value5,
                onValueChange = { value5 = it ?: 0.0 },
                enabled = true
            )
        }
    }
}

