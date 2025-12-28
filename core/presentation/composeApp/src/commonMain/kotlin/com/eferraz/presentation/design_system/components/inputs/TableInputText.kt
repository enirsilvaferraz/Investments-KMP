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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun TableInputText(
    value: String,
    onChange: (String) -> Unit,
) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputText(
        value = value,
        isError = isError,
        onValueChange = {
            setValue(it)
            setError(false)
            runCatching { onChange(it) }.getOrElse { setError(true) }
        }
    )
}

@Composable
internal fun TableInputText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }
    val isHoveredState by actualInteractionSource.collectIsHoveredAsState()
    val isFocusedState by actualInteractionSource.collectIsFocusedAsState()

    TableInputText(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        actualInteractionSource = actualInteractionSource,
        isHovered = isHoveredState,
        isFocused = isFocusedState
    )
}

@Composable
private fun TableInputText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    actualInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean = false,
    isFocused: Boolean = false,
) {

    // Mantém o estado interno do texto com a seleção do cursor
    var textFieldValueState: TextFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    // Sincroniza quando o valor externo muda
    LaunchedEffect(value) {
        if (textFieldValueState.text != value) {
            // Preserva a posição do cursor se possível, caso contrário coloca no final
            val currentSelection = textFieldValueState.selection
            val newLength = value.length

            val newSelection = if (currentSelection.start <= newLength && currentSelection.end <= newLength) {
                currentSelection
            } else {
                TextRange(newLength)
            }

            textFieldValueState = TextFieldValue(text = value, selection = newSelection)
        }
    }

    val colors = MaterialTheme.colorScheme
    val textColor = when {
        isError -> colors.error
        enabled -> colors.onSurface
        else -> colors.onSurfaceVariant
    }

    fun onValueChange(newValue: TextFieldValue) {
        // Atualiza o estado interno mantendo a seleção do cursor
        textFieldValueState = newValue

        // Atualiza o valor externo
        onValueChange(newValue.text)
    }

    TableInputLookAndFeel(
        modifier = modifier,
        actualInteractionSource = actualInteractionSource,
        enabled = enabled,
        isHovered = isHovered,
        isFocused = isFocused,
        isError = isError
    ) {

        BasicTextField(
            value = textFieldValueState,
            enabled = enabled,
            onValueChange = { newValue -> onValueChange(newValue) },
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            interactionSource = actualInteractionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
        )
    }
}

@Preview
@Composable
private fun TableInputTextPreview() {

    Surface {

        // Estado Normal (sem hover, sem focus)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Normal", style = MaterialTheme.typography.labelSmall)
            var value1 by remember { mutableStateOf("Texto de exemplo") }
            TableInputText(
                value = value1,
                onValueChange = { value1 = it },
                enabled = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputTextHoverPreview() {

    Surface {

        // Estado Hover (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Hover", style = MaterialTheme.typography.labelSmall)
            var value2 by remember { mutableStateOf("Texto de exemplo") }
            TableInputText(
                value = value2,
                onValueChange = { value2 = it },
                enabled = true,
                isHovered = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputTextFocusedPreview() {

    Surface {

        // Estado Focused (simulado)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Focused", style = MaterialTheme.typography.labelSmall)
            var value3 by remember { mutableStateOf("Texto de exemplo") }
            TableInputText(
                value = value3,
                onValueChange = { value3 = it },
                enabled = true,
                isFocused = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputTextDisabledPreview() {

    Surface {

        // Estado Disabled
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Disabled", style = MaterialTheme.typography.labelSmall)
            TableInputText(
                value = "Texto de exemplo",
                onValueChange = {},
                enabled = false
            )
        }
    }
}

@Preview
@Composable
private fun TableInputTextEmptyPreview() {

    Surface {

        // Estado vazio
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Vazio", style = MaterialTheme.typography.labelSmall)
            var value5 by remember { mutableStateOf("") }
            TableInputText(
                value = value5,
                onValueChange = { value5 = it },
                enabled = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputTextErrorPreview() {

    Surface {

        // Estado de Erro
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Erro", style = MaterialTheme.typography.labelSmall)
            var value6 by remember { mutableStateOf("Texto de exemplo") }
            TableInputText(
                value = value6,
                onValueChange = { value6 = it },
                enabled = true,
                isError = true
            )
        }
    }
}

