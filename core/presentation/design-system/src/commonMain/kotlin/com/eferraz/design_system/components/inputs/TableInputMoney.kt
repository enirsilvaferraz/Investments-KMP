package com.eferraz.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.components.inputs.state.rememberMoneyInputState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop

@OptIn(FlowPreview::class)
@Composable
public fun TableInputMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state = rememberMoneyInputState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val colors = MaterialTheme.colorScheme
    val textColor = if (enabled) colors.onSurface else colors.onSurfaceVariant

    LaunchedEffect(Unit) {
        snapshotFlow { state.currentAmount }
            .drop(1)
            .debounce(350L)
            .collect { currentOnValueChange(it) }
    }

    LaunchedEffect(value) {
        state.syncWithExternalValue(value)
    }

    TableInputLookAndFeel(
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        isError = isError,
    ) {
        BasicTextField(
            value = state.textFieldValue,
            enabled = enabled,
            onValueChange = { state.onValueChange(it) },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.End,
                color = textColor,
            ),
            visualTransformation = CurrencyVisualTransformation,
        )
    }
}

private object CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.trimStart { it == '0' }
        val amount = if (digits.isEmpty()) 0.0 else digits.toDouble() / 100
        val cents = (amount * 100).toInt()
        val integerPart = cents / 100
        val decimalPart = (cents % 100).toString().padStart(2, '0')
        val grouped = integerPart
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        val formatted = "R$ $grouped,$decimalPart"

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.text.length
            },
        )
    }
}
