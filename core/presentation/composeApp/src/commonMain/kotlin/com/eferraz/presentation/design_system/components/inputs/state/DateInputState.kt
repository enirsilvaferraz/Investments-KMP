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
import com.eferraz.design_system.input.date.filterDateMaskDigits

@Stable
internal class DateInputState(
    initialValue: String
) {
    var textFieldValue by mutableStateOf(initialValueToTextFieldValue(initialValue))
        private set

    private fun initialValueToTextFieldValue(value: String): TextFieldValue {
        val digits = filterDateMaskDigits(value)
        return TextFieldValue(text = digits, selection = TextRange(digits.length))
    }

    fun onValueChange(newValue: TextFieldValue, onExternalChange: (String) -> Unit) {
        val limitedText = filterDateMaskDigits(newValue.text)

        textFieldValue = TextFieldValue(
            text = limitedText,
            selection = TextRange(limitedText.length)
        )

        onExternalChange(limitedText)
    }

    fun syncWithExternalValue(value: String) {
        val currentDigits = filterDateMaskDigits(textFieldValue.text)
        val externalDigits = filterDateMaskDigits(value)

        if (currentDigits != externalDigits) {
            textFieldValue = TextFieldValue(text = externalDigits, selection = TextRange(externalDigits.length))
        }
    }

    companion object {
        val Saver: Saver<DateInputState, String> = Saver(
            save = { state -> state.textFieldValue.text },
            restore = { DateInputState(it) }
        )
    }
}

@Composable
internal fun rememberDateInputState(initialValue: String): DateInputState =
    rememberSaveable(saver = DateInputState.Saver) {
        DateInputState(initialValue)
    }
