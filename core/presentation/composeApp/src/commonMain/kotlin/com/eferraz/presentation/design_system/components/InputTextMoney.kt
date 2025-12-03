package com.eferraz.presentation.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.helpers.CurrencyVisualTransformation

@Composable
internal fun InputTextMoney(
    value: Double,
    onValueChange: (Double?) -> Unit,
    enabled: Boolean,
) {

    var textFieldValueState: TextFieldValue by remember(value) {
        val textValue = (value * 100).toInt().toString()
        mutableStateOf(
            TextFieldValue(
                text = textValue,
                selection = TextRange(textValue.length)
            )
        )
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .then(
                if (enabled) Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                else Modifier
            )
            .fillMaxWidth()
            .height(35.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = textFieldValueState,
            enabled = enabled,
            onValueChange = {

                // Filtra apenas dígitos (0-9)
                val filteredText = it.text.filter { char -> char.isDigit() }

                // Mantém a posição do cursor ajustada após a filtragem
                val cursorOffset = if (filteredText.length < it.text.length) {
                    // Se caracteres foram removidos, ajusta o cursor
                    minOf(it.selection.start.coerceIn(0, filteredText.length), filteredText.length)
                } else {
                    // Se não houve remoção, mantém a posição relativa
                    val removedBeforeCursor = it.text.take(it.selection.start).count { !it.isDigit() }
                    (it.selection.start - removedBeforeCursor).coerceIn(0, filteredText.length)
                }

                val newTextFieldValue = TextFieldValue(
                    text = filteredText,
                    selection = TextRange(cursorOffset)
                )

                textFieldValueState = newTextFieldValue

                val string = filteredText.trimStart { it == '0' }

                if (string.isEmpty()) onValueChange(null)
                else onValueChange(string.toDouble() / 100)

            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
            visualTransformation = CurrencyVisualTransformation
        )
    }
}