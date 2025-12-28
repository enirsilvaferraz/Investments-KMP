package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun <T> TableInputSelect(
    value: T,
    options: List<T>,
    format: (T) -> String,
    onChange: (T) -> Unit,
) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputSelect(
        value = value,
        isError = isError,
        onValueChange = {
            setValue(it)
            setError(false)
            runCatching { onChange(it) }.getOrElse { setError(true) }
        },
        options = options,
        format = format
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun <T> TableInputSelect(
    value: T?,
    onValueChange: (T) -> Unit,
    options: List<T>,
    format: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    placeholder: String? = null,
) {

    val actualInteractionSource = remember { MutableInteractionSource() }
    val isHoveredState by actualInteractionSource.collectIsHoveredAsState()
    val isFocusedState by actualInteractionSource.collectIsFocusedAsState()

    TableInputSelect(
        value = value,
        onValueChange = onValueChange,
        options = options,
        format = format,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        placeholder = placeholder,
        actualInteractionSource = actualInteractionSource,
        isHovered = isHoveredState,
        isFocused = isFocusedState
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun <T> TableInputSelect(
    value: T?,
    onValueChange: (T) -> Unit,
    options: List<T>,
    format: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    placeholder: String? = null,
    actualInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean = false,
    isFocused: Boolean = false,
) {

    var expanded by remember { mutableStateOf(false) }

    val displayValue = value?.let { format(it) } ?: (placeholder ?: "")

    // Mostra a seta apenas quando as bordas estão visíveis (hover, focus ou error)
    val showBorder = (isHovered || isFocused || isError) && enabled

    val colors = MaterialTheme.colorScheme
    val textColor = when {
        isError -> colors.error
        enabled -> colors.onSurface
        else -> colors.onSurfaceVariant
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {

        TableInputLookAndFeel(
            modifier = Modifier.menuAnchor(),
            actualInteractionSource = actualInteractionSource,
            enabled = enabled,
            isHovered = isHovered,
            isFocused = isFocused,
            isError = isError
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                BasicTextField(
                    value = displayValue,
                    onValueChange = { },
                    enabled = enabled,
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    interactionSource = actualInteractionSource,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                )

                if (showBorder) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = format(option),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TableInputSelectPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Normal", style = MaterialTheme.typography.labelSmall)
            var value1 by remember { mutableStateOf<TestOption?>(TestOption(1, "Opção 1")) }
            TableInputSelect(
                value = value1,
                onValueChange = { value1 = it },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputSelectHoverPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Hover", style = MaterialTheme.typography.labelSmall)
            var value2 by remember { mutableStateOf<TestOption?>(TestOption(1, "Opção 1")) }
            TableInputSelect(
                value = value2,
                onValueChange = { value2 = it },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = true,
                isHovered = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputSelectFocusedPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Focused", style = MaterialTheme.typography.labelSmall)
            var value3 by remember { mutableStateOf<TestOption?>(TestOption(1, "Opção 1")) }
            TableInputSelect(
                value = value3,
                onValueChange = { value3 = it },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = true,
                isFocused = true
            )
        }
    }
}

@Preview
@Composable
private fun TableInputSelectDisabledPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Disabled", style = MaterialTheme.typography.labelSmall)
            TableInputSelect(
                value = TestOption(1, "Opção 1"),
                onValueChange = { },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = false
            )
        }
    }
}

@Preview
@Composable
private fun TableInputSelectEmptyPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Vazio", style = MaterialTheme.typography.labelSmall)
            var value5 by remember { mutableStateOf<TestOption?>(null) }
            TableInputSelect(
                value = value5,
                onValueChange = { value5 = it },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = true,
                placeholder = "Selecione uma opção"
            )
        }
    }
}

@Preview
@Composable
private fun TableInputSelectErrorPreview() {

    data class TestOption(val id: Int, val name: String)

    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text("Erro", style = MaterialTheme.typography.labelSmall)
            var value6 by remember { mutableStateOf<TestOption?>(TestOption(1, "Opção 1")) }
            TableInputSelect(
                value = value6,
                onValueChange = { value6 = it },
                options = listOf(
                    TestOption(1, "Opção 1"),
                    TestOption(2, "Opção 2"),
                    TestOption(3, "Opção 3")
                ),
                format = { it.name },
                enabled = true,
                isError = true
            )
        }
    }
}

