package com.eferraz.presentation.helpers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

internal object CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val string = text.text.trimStart { it == '0' }
        val inDouble = if (string.isEmpty()) 0.0 else string.toDouble() / 100
        val formattedValue = inDouble.currencyFormat()

        return TransformedText(
            text = AnnotatedString(text = formattedValue),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formattedValue.length
                override fun transformedToOriginal(offset: Int): Int = text.text.length
            }
        )
    }
}