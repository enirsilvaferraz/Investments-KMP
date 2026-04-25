package com.eferraz.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation

@Composable
public fun TableInputDate(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var current by remember(value) { mutableStateOf(value.filter(Char::isDigit)) }
    val colors = MaterialTheme.colorScheme
    val textColor = when {
        isError -> colors.error
        enabled -> colors.onSurface
        else -> colors.onSurfaceVariant
    }
    val visualTransformation: VisualTransformation = remember(dateFormat) {
        DateVisualTransformation(dateFormat)
    }

    TableInputLookAndFeel(
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        isError = isError,
    ) {
        BasicTextField(
            value = current,
            enabled = enabled,
            onValueChange = {
                val filtered = it.filter(Char::isDigit).take(8)
                current = filtered
                onValueChange(filtered)
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(92.dp),
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            visualTransformation = visualTransformation,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}
