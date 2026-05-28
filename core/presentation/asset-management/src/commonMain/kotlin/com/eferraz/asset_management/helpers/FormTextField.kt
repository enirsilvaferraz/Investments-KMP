package com.eferraz.asset_management.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
internal fun FormTextField(
    label: String= "",
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions? = null,
    visualTransformation: VisualTransformation? = null,
    supportingTextWhenNoError: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
) {

    val supportingText = if (errorMessage != null) {
        { Text(errorMessage) }
    } else {
        supportingTextWhenNoError
    }

    Column(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

       label.takeIf { it.isNotBlank() }?.let {
           Text(
               text = label,
               style = MaterialTheme.typography.titleSmall,
               color = MaterialTheme.colorScheme.onSurface,
           )
       }

        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = placeholder,
            modifier = Modifier.fillMaxSize(),
            enabled = !readOnly,
            isError = errorMessage != null,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
            visualTransformation = visualTransformation ?: VisualTransformation.None,
            shape = RoundedCornerShape(14.dp),
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEFF0F3),
                unfocusedContainerColor = Color(0xFFEFF0F3),
                disabledContainerColor = Color(0xFFEFF0F3),
                errorContainerColor = Color(0xFFEFF0F3),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
        )
    }
}