package com.eferraz.presentation.design_system.components.inputs.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.abs

@Stable
internal class MoneyInputState(
    initialValue: Double
) {
    var textFieldValue by mutableStateOf(
        TextFieldValue(
            text = (initialValue * 100).toInt().toString(),
            selection = TextRange((initialValue * 100).toInt().toString().length)
        )
    )
        private set

    fun onValueChange(newValue: TextFieldValue, onExternalChange: (Double?) -> Unit) {
        val filteredText = newValue.text.filter { char -> char.isDigit() }

        val cursorOffset = if (filteredText.length < newValue.text.length) {
            minOf(newValue.selection.start.coerceIn(0, filteredText.length), filteredText.length)
        } else {
            val removedBeforeCursor = newValue.text.take(newValue.selection.start).count { !it.isDigit() }
            (newValue.selection.start - removedBeforeCursor).coerceIn(0, filteredText.length)
        }

        textFieldValue = TextFieldValue(
            text = filteredText,
            selection = TextRange(cursorOffset)
        )

        val string = filteredText.trimStart { it == '0' }
        if (string.isEmpty()) {
            onExternalChange(null)
        } else {
            onExternalChange(string.toDouble() / 100)
        }
    }

    fun syncWithExternalValue(value: Double) {
        val currentTextValue = textFieldValue.text.trimStart { it == '0' }
        val currentValue = if (currentTextValue.isEmpty()) 0.0 else currentTextValue.toDouble() / 100

        if (abs(currentValue - value) > 0.001) {
            val textValue = (value * 100).toInt().toString()
            textFieldValue = TextFieldValue(
                text = textValue,
                selection = TextRange(textValue.length)
            )
        }
    }

    companion object {
        val Saver: Saver<MoneyInputState, Double> = Saver(
            save = { state ->
                val text = state.textFieldValue.text.trimStart { it == '0' }
                if (text.isEmpty()) 0.0 else text.toDouble() / 100
            },
            restore = { MoneyInputState(it) }
        )
    }
}

@Composable
internal fun rememberMoneyInputState(initialValue: Double): MoneyInputState {
    return rememberSaveable(saver = MoneyInputState.Saver) {
        MoneyInputState(initialValue)
    }
}
