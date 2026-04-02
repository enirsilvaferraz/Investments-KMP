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
import com.eferraz.design_system.core.StableList

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun <T> SegmentedControl(
    modifier: Modifier = Modifier,
    selected: SegmentedControlChoice<T>?,
    options: StableList<SegmentedControlChoice<T>>,
    onSelect: (SegmentedControlChoice<T>) -> Unit,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    fill: Boolean = false,
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {

        options.items.forEachIndexed { index, option ->

            val textLength = option.label.length
            val baseWeight = 3f // peso mínimo para todos
            val proportionalWeight = textLength * 0.5f // fator de proporção reduzido

            val internalModifier = if (fill) Modifier.weight(baseWeight + proportionalWeight) else Modifier

            ToggleButton(
                checked = selected == option,
                onCheckedChange = { onSelect(option) },
                modifier = internalModifier.semantics { role = Role.RadioButton },
                colors = colors,
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(
                    text = option.label,
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

                var r1 by remember { mutableStateOf("Renda Variável".let { SegmentedControlChoice(it, it) }) }

                SegmentedControl(
                    selected = r1,
                    options = StableList(
                        listOf("Renda Fixa", "Renda Variável", "Fundos").map { SegmentedControlChoice(it, it) }
                    ),
                    onSelect = { r1 = it }
                )

                var r2 by remember { mutableStateOf("Liquidez Diária".let { SegmentedControlChoice(it, it) }) }

                SegmentedControl(
                    selected = r2,
                    options = StableList(
                        listOf("Liquidez Diária", "No Vencimento").map { SegmentedControlChoice(it, it) }
                    ),
                    onSelect = { r2 = it },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                var r3 by remember { mutableStateOf("Fundos".let { SegmentedControlChoice(it, it) }) }

                SegmentedControl(
                    selected = r3,
                    options = StableList(
                        listOf("Renda Fixa", "Renda Variável", "Fundos").map { SegmentedControlChoice(it, it) }
                    ),
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
