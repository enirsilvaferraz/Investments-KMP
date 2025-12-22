package com.eferraz.presentation.design_system.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions? = null,
    maxLines: Int = 1,
    validationErrors: Map<String, String>? = null,
    errorKey: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
) {
    val errorMessage = validationErrors?.let { errors ->
        errorKey?.let { errors[it] }
    }
    val isError = errorMessage != null

    OutlinedTextField(
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        visualTransformation = visualTransformation,
        enabled = enabled,
        readOnly = !enabled,
        modifier = modifier
    )
}

