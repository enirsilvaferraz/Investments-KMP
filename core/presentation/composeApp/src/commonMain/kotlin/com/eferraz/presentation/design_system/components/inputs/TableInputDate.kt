package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.presentation.design_system.components.inputs.state.rememberDateInputState

@Composable
internal fun TableInputDate(value: String, onChange: (String) -> Unit,) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputDate(
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
internal fun TableInputDate(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    enabled: Boolean = true,
    isError: Boolean = false,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }

    TableInputDate(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        dateFormat = dateFormat,
        enabled = enabled,
        isError = isError,
        interactionSource = actualInteractionSource
    )
}

@Composable
private fun TableInputDate(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean? = null,
    isFocused: Boolean? = null,
) {

    val state = rememberDateInputState(value)

    LaunchedEffect(value) {
        state.syncWithExternalValue(value)
    }

    val colors = MaterialTheme.colorScheme
    val textColor = when {
        isError -> colors.error
        enabled -> colors.onSurface
        else -> colors.onSurfaceVariant
    }

    val visualTransformation = remember(dateFormat) {
        DateVisualTransformation(dateFormat)
    }

    TableInputLookAndFeel(
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        isHovered = isHovered,
        isFocused = isFocused,
        isError = isError
    ) {

        BasicTextField(
            value = state.textFieldValue,
            enabled = enabled,
            onValueChange = { newValue -> state.onValueChange(newValue, onValueChange) },
            modifier = Modifier.padding(horizontal = 8.dp).width(85.dp),
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            visualTransformation = visualTransformation
        )
    }
}

@Preview
@Composable
private fun TableInputDatePreview() {

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
private fun TableInputDateHoverPreview() {

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
private fun TableInputDateFocusedPreview() {

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
private fun TableInputDateDisabledPreview() {

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
private fun TableInputDateDDMMYYYPreview() {

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
private fun TableInputDateEmptyPreview() {

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

@Preview
@Composable
private fun TableInputDateErrorPreview() {

    Surface {

        // Estado de Erro
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Erro", style = MaterialTheme.typography.labelSmall)
            var value6 by remember { mutableStateOf("20240115") }
            TableInputDate(
                value = value6,
                onValueChange = { value6 = it },
                dateFormat = DateFormat.YYYY_MM_DD,
                enabled = true,
                isError = true
            )
        }
    }
}
