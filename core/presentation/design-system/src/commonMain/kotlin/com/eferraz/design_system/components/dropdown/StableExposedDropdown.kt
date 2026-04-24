package com.eferraz.design_system.components.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Dropdown só leitura (Material 3 [ExposedDropdownMenuBox]).
 *
 * Pré-visualizações Compose no final deste ficheiro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <T> StableExposedDropdown(
    label: String,
    displayValue: String,
    options: List<T>,
    itemLabel: (T) -> String,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    required: Boolean = false,
    error: String? = null,
) {
    StableExposedDropdownImpl(
        label = label,
        displayValue = displayValue,
        options = options,
        itemLabel = itemLabel,
        modifier = modifier,
        enabled = enabled,
        error = error,
        required = required,
        onItemSelect = { value -> onItemSelect(value!!) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> StableExposedDropdownImpl(
    label: String,
    displayValue: String,
    options: List<T>,
    itemLabel: (T) -> String,
    enabled: Boolean,
    error: String?,
    required: Boolean = false,
    onItemSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {

        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = enabled,
                )
                .fillMaxWidth(),
            readOnly = true,
            value = displayValue,
            onValueChange = {},
            label = { Text(label) },
            enabled = enabled,
            isError = error != null,
            supportingText = error?.let { { Text(it) } } ?: { if (required) Text("Obrigatório") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = MenuDefaults.TonalElevation,
            shadowElevation = MenuDefaults.ShadowElevation,
        ) {

            val collapseAndSelect: (T?) -> Unit = { value ->
                expanded = false
                onItemSelect(value)
            }

            StableExposedDropdownMenuList {
                options.forEach { item ->
                    StableExposedDropdownMenuRow(
                        text = itemLabel(item),
                        isSelected = itemLabel(item) == displayValue,
                        onClick = { collapseAndSelect(item) },
                    )
                }
            }
        }
    }
}

/**
 * Menu vertical M3 Expressive (standard): espaçamento entre itens e alinhamento ao painel arredondado.
 *
 * Referência: [Menus – Material Design 3](https://m3.material.io/components/menus/specs)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StableExposedDropdownMenuList(
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StableExposedDropdownMenuRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val itemShape = MaterialTheme.shapes.medium
    DropdownMenuItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = itemShape,
                    )
                } else {
                    Modifier
                },
            ),
        text = {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        },
        onClick = onClick,
        colors = if (isSelected) {
            MenuDefaults.itemColors().copy(
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                trailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        } else {
            MenuDefaults.itemColors()
        },
        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun StableExposedDropdownPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var selected by remember { mutableStateOf("Pré-fixado") }
            StableExposedDropdown(
                label = "Tipo de cálculo",
                displayValue = selected,
                options = listOf("Pré-fixado", "Pós-fixado", "Atrelado à inflação"),
                itemLabel = { it },
                onItemSelect = { selected = it },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun StableExposedDropdownErrorPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            StableExposedDropdown(
                label = "Campo com erro",
                displayValue = "",
                options = listOf("A", "B"),
                itemLabel = { it },
                onItemSelect = {},
                error = "Selecione uma opção",
            )
        }
    }
}