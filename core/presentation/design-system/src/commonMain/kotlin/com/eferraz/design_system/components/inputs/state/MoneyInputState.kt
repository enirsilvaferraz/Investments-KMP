package com.eferraz.design_system.components.inputs.state

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
    initialValue: Double,
) {
    var textFieldValue: TextFieldValue by mutableStateOf(
        TextFieldValue(
            text = (initialValue * 100).toInt().toString(),
            selection = TextRange((initialValue * 100).toInt().toString().length),
        )
    )
        private set

    var currentAmount: Double? by mutableStateOf(if ((initialValue * 100).toInt() == 0) null else initialValue)
        private set

    internal fun onValueChange(newValue: TextFieldValue) {
        val filteredText = newValue.text.filter { it.isDigit() }
        val cursor = newValue.selection.start.coerceIn(0, filteredText.length)
        textFieldValue = TextFieldValue(
            text = filteredText,
            selection = TextRange(cursor),
        )

        val number = filteredText.trimStart { it == '0' }
        currentAmount = if (number.isEmpty()) null else number.toDouble() / 100
    }

    internal fun syncWithExternalValue(value: Double) {
        val currentText = textFieldValue.text.trimStart { it == '0' }
        val current = if (currentText.isEmpty()) 0.0 else currentText.toDouble() / 100
        if (abs(current - value) > 0.001) {
            val newText = (value * 100).toInt().toString()
            textFieldValue = TextFieldValue(newText, selection = TextRange(newText.length))
        }
    }

    internal companion object {
        internal val Saver: Saver<MoneyInputState, Double> = Saver(
            save = { state ->
                val text = state.textFieldValue.text.trimStart { it == '0' }
                if (text.isEmpty()) 0.0 else text.toDouble() / 100
            },
            restore = { MoneyInputState(it) },
        )
    }
}

@Composable
internal fun rememberMoneyInputState(initialValue: Double): MoneyInputState =
    rememberSaveable(saver = MoneyInputState.Saver) {
        MoneyInputState(initialValue)
    }
