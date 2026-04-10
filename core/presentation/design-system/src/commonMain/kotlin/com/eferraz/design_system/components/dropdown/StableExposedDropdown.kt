package com.eferraz.design_system.components.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.core.StableList

/**
 * Dropdown só leitura (Material 3 [ExposedDropdownMenuBox]) com opções estáveis para o Compose.
 *
 * Pré-visualizações Compose no final deste ficheiro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <T> StableExposedDropdown(
    label: String,
    displayValue: String,
    options: StableList<T>,
    itemLabel: (T) -> String,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
        nullOptionLabel = null,
        onItemSelect = { value -> onItemSelect(value!!) },
    )
}

/**
 * Igual a [StableExposedDropdown], com primeira linha opcional que envia `null` (ex.: “nenhum emissor”).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <T> StableExposedDropdownWithNull(
    label: String,
    displayValue: String,
    options: StableList<T>,
    itemLabel: (T) -> String,
    onItemSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    nullOptionLabel: String = "—",
) {
    StableExposedDropdownImpl(
        label = label,
        displayValue = displayValue,
        options = options,
        itemLabel = itemLabel,
        modifier = modifier,
        enabled = enabled,
        error = error,
        nullOptionLabel = nullOptionLabel,
        onItemSelect = onItemSelect,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> StableExposedDropdownImpl(
    label: String,
    displayValue: String,
    options: StableList<T>,
    itemLabel: (T) -> String,
    enabled: Boolean,
    error: String?,
    nullOptionLabel: String?,
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
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {

            val collapseAndSelect: (T?) -> Unit = { value ->
                expanded = false
                onItemSelect(value)
            }

            nullOptionLabel?.let { nullLabel ->
                StableExposedDropdownMenuRow(text = nullLabel, onClick = { collapseAndSelect(null) })
            }

            options.items.forEach { item ->
                StableExposedDropdownMenuRow(
                    text = itemLabel(item),
                    onClick = { collapseAndSelect(item) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StableExposedDropdownMenuRow(
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
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
                options = StableList(listOf("Pré-fixado", "Pós-fixado", "Atrelado à inflação")),
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
                options = StableList(listOf("A", "B")),
                itemLabel = { it },
                onItemSelect = {},
                error = "Selecione uma opção",
            )
        }
    }
}

private data class StableExposedDropdownPreviewIssuer(val id: Long, val name: String)

@Preview(showBackground = true, widthDp = 360)
@Composable
internal fun StableExposedDropdownWithNullPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {

            var selected: StableExposedDropdownPreviewIssuer? by remember { mutableStateOf(null) }

            StableExposedDropdownWithNull(
                label = "Emissor (cadastro)",
                displayValue = selected?.name.orEmpty(),
                options = StableList(
                    listOf(
                        StableExposedDropdownPreviewIssuer(1, "Banco Exemplo"),
                        StableExposedDropdownPreviewIssuer(2, "Tesouro Nacional"),
                    ),
                ),
                itemLabel = { it.name },
                onItemSelect = { selected = it },
            )
        }
    }
}
