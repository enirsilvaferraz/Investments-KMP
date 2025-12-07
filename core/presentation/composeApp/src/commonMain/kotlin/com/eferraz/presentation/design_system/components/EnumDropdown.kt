package com.eferraz.presentation.design_system.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> EnumDropdown(
    value: T?,
    onValueChange: (T) -> Unit,
    options: List<T?>,
    getLabel: (T?) -> String,
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onNullSelected: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = getLabel(value)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            enabled = enabled,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it) } },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(getLabel(option)) },
                    onClick = {
                        if (option != null) {
                            onValueChange(option)
                        } else {
                            onNullSelected?.invoke()
                        }
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

