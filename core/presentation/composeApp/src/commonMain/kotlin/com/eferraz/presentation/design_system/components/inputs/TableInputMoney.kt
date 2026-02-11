package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.components.inputs.state.rememberMoneyInputState
import com.eferraz.presentation.helpers.CurrencyVisualTransformation
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop

@Composable
internal fun TableInputMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }

    TableInputMoney(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = actualInteractionSource
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun TableInputMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean? = null,
    isFocused: Boolean? = null,
) {

    val state = rememberMoneyInputState(value)
    val isFocusedState = interactionSource.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        // Observa mudanças no valor digitado e no estado de foco
        snapshotFlow { Pair(state.currentAmount, isFocusedState.value) }
            .drop(1) // Ignora o valor inicial para evitar disparos desnecessários ao montar
            .debounce { (_, isFocused) ->
                // Se estiver focado, espera 3000ms antes de emitir (Debounce)
                // Se perder o foco, emite imediatamente (0ms)
                if (isFocused) 3000L else 0L
            }
            .collect { (amount, _) ->
                // Notifica a mudança apenas após o debounce ou perda de foco
                onValueChange(amount)
            }
    }

    LaunchedEffect(value) {
        state.syncWithExternalValue(value)
    }

    val colors = MaterialTheme.colorScheme
    val textColor = if (enabled) colors.onSurface else colors.onSurfaceVariant

    TableInputLookAndFeel(
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        isHovered = isHovered,
        isFocused = isFocused
    ) {

        BasicTextField(
            value = state.textFieldValue,
            enabled = enabled,
            onValueChange = { newValue -> state.onValueChange(newValue) },
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
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

