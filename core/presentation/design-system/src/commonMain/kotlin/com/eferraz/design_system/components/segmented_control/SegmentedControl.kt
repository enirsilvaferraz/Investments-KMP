package com.eferraz.design_system.components.segmented_control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun <T> SegmentedControl(
    modifier: Modifier = Modifier,
    selected: T?,
    options: List<T>,
    onSelect: (T) -> Unit,
    optionDisplay: (T) -> String = { it.toString() },
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {

        options.forEachIndexed { index, option ->

            val textLength = optionDisplay(option).length
            val baseWeight = 3f  // peso mínimo para todos
            val proportionalWeight = textLength * 0.5f  // fator de proporção reduzido

            ToggleButton(
                checked = selected == option,
                onCheckedChange = { onSelect(option) },
                modifier = Modifier.weight(baseWeight + proportionalWeight).semantics { role = Role.RadioButton },
                colors = colors,
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(
                    text = optionDisplay(option),
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun SegmentedControlPreview() {
    MaterialTheme {
        Surface {
            Column {

                var r1 by remember { mutableStateOf("Renda Variável") }

                SegmentedControl(
                    selected = r1,
                    options = listOf("Renda Fixa", "Renda Variável", "Fundos"),
                    onSelect = { r1 = it }
                )

                var r2 by remember { mutableStateOf("Liquidez Diária") }

                SegmentedControl(
                    selected = r2,
                    options = listOf("Liquidez Diária", "No Vencimento"),
                    onSelect = { r2 = it },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                var r3 by remember { mutableStateOf("Fundos") }

                SegmentedControl(
                    selected = r3,
                    options = listOf("Renda Fixa", "Renda Variável", "Fundos"),
                    onSelect = { r3 = it },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
}