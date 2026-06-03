package com.eferraz.design_system_v2.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.theme.AppThemeV2

@Immutable
public data class FilterToggleOption<T>(
    val id: T,
    val label: String,
    val contentDescription: String = label,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun <T> FilterToggleGroup(
    options: List<FilterToggleOption<T>>,
    selectedIds: Set<T>,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (options.isEmpty()) return

    val colors = FilterChipDefaults.filterChipColors()

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FilterToggleGroupDefaults.ChipSpacing),
    ) {
        options.forEach { option ->
            FilterToggleChipWithOptionalTooltip(
                option = option,
                selected = option.id in selectedIds,
                onClick = { onToggle(option.id) },
                enabled = enabled,
                colors = colors,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterToggleChipWithOptionalTooltip(
    option: FilterToggleOption<T>,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    colors: SelectableChipColors,
    modifier: Modifier = Modifier,
) {
    val showTooltip = option.contentDescription != option.label

    @Composable
    fun Chip(chipModifier: Modifier = Modifier) {
        FilterToggleChip(
            option = option,
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            modifier = chipModifier,
        )
    }

    if (showTooltip) {
        TooltipBox(
            positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Below,
                ),
            tooltip = {
                PlainTooltip {
                    Text(option.contentDescription)
                }
            },
            state = rememberTooltipState(),
            modifier = modifier,
        ) {
            Chip()
        }
    } else {
        Chip(chipModifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterToggleChip(
    option: FilterToggleOption<T>,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    colors: SelectableChipColors,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = option.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        },
        modifier =
            modifier.semantics {
                role = Role.Checkbox
                contentDescription = option.contentDescription
                if (selected) {
                    stateDescription = "Selecionado"
                }
            }.width(IntrinsicSize.Max),
        colors = colors,
        border = FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected),
        elevation = null, // Evita highlight rectangular no hover em Compose Desktop (CMP #2868).
        contentPadding = PaddingValues(horizontal = 2.dp)
    )
}

private data class FilterToggleGroupPreviewCase(
    val options: List<FilterToggleOption<String>>,
    val initialSelected: Set<String>,
)

private class FilterToggleGroupPreviewProvider : PreviewParameterProvider<FilterToggleGroupPreviewCase> {

    override val values: Sequence<FilterToggleGroupPreviewCase> =
        sequenceOf(
            FilterToggleGroupPreviewCase(
                options = listOf(
                    FilterToggleOption("RF", "Renda Fixa"),
                    FilterToggleOption("RV", "Renda Variável"),
                    FilterToggleOption("F", "Fundos"),
                ),
                initialSelected = setOf("RF", "RV"),
            ),
            FilterToggleGroupPreviewCase(
                options = listOf(
                    FilterToggleOption("D1", "D+1"),
                    FilterToggleOption("DI", "Diária"),
                    FilterToggleOption("NV", "No Vencimento"),
                ),
                initialSelected = setOf("D1"),
            ),
            FilterToggleGroupPreviewCase(
                options = listOf(
                    FilterToggleOption("Sim", "Sim", contentDescription = "B3 informado: Sim"),
                    FilterToggleOption("Nao", "Não", contentDescription = "B3 informado: Não"),
                ),
                initialSelected = setOf(),
            ),
        )
}

@Preview(name = "Light", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun FilterToggleGroupPreview(
    @PreviewParameter(FilterToggleGroupPreviewProvider::class) previewCase: FilterToggleGroupPreviewCase,
) {
    AppThemeV2 {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                var selected by remember(previewCase) { mutableStateOf(previewCase.initialSelected) }

                FilterToggleGroup(
                    options = previewCase.options,
                    selectedIds = selected,
                    onToggle = { id ->
                        selected = if (id in selected) selected - id else selected + id
                    },
                )
            }
        }
    }
}
