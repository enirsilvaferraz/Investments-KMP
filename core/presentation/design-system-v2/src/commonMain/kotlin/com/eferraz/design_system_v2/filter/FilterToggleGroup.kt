package com.eferraz.design_system_v2.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.theme.AppThemeV2

@Immutable
public data class FilterToggleOption<T>(
    val id: T,
    val label: String,
    val contentDescription: String = label,
)

public enum class FilterToggleSize {
    Standard,
    Small,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun <T> FilterToggleGroup(
    options: List<FilterToggleOption<T>>,
    selectedIds: Set<T>,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: FilterToggleSize = FilterToggleSize.Small,
    enabled: Boolean = true,
) {

    if (options.isEmpty()) return

    val colors = ToggleButtonDefaults.toggleButtonColors()
    val textStyle = FilterToggleGroupDefaults.textStyle(size)
    val toggleHeight = FilterToggleGroupDefaults.toggleHeight(size)

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {

        options.forEachIndexed { index, option ->

            FilterToggleButtonWithOptionalTooltip(
                option = option,
                checked = option.id in selectedIds,
                onCheckedChange = { onToggle(option.id) },
                enabled = enabled,
                indexInRow = index,
                rowSize = options.size,
                colors = colors,
                textStyle = textStyle,
                toggleHeight = toggleHeight,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterToggleButtonWithOptionalTooltip(
    option: FilterToggleOption<T>,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    indexInRow: Int,
    rowSize: Int,
    colors: ToggleButtonColors,
    textStyle: TextStyle,
    toggleHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val showTooltip = option.contentDescription != option.label

    @Composable
    fun Button() {
        FilterToggleButton(
            option = option,
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            indexInRow = indexInRow,
            rowSize = rowSize,
            colors = colors,
            textStyle = textStyle,
            toggleHeight = toggleHeight
        )
    }

    if (showTooltip) {

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Below),
            tooltip = {
                PlainTooltip {
                    Text(option.contentDescription)
                }
            },
            state = rememberTooltipState(),
            modifier = modifier,
        ) {
            Button()
        }

    } else {

        Box(
            modifier = modifier
        ) {
            Button()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> FilterToggleButton(
    option: FilterToggleOption<T>,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    indexInRow: Int,
    rowSize: Int,
    colors: ToggleButtonColors,
    textStyle: TextStyle,
    toggleHeight: Dp,
    modifier: Modifier = Modifier,
) {

    val touchPaddingVertical =
        ((FilterToggleGroupDefaults.MinTouchTarget - toggleHeight) / 2).coerceAtLeast(0.dp)

    ToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .padding(vertical = touchPaddingVertical)
                .height(toggleHeight)
                .defaultMinSize(minWidth = FilterToggleGroupDefaults.MinTouchTarget)
                .semantics {
                    role = Role.Checkbox
                    contentDescription = option.contentDescription
                    if (checked) {
                        stateDescription = "Selecionado"
                    }
                },
        colors = colors,
        shapes = connectedShapesForIndex(indexInRow, rowSize, toggleHeight),
    ) {
        Text(
            text = option.label,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedShapesForIndex(
    indexInRow: Int,
    rowSize: Int,
    toggleHeight: Dp,
) = when {
    rowSize == 1 -> ToggleButtonDefaults.shapesFor(toggleHeight)
    indexInRow == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    indexInRow == rowSize - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
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
            FilterToggleGroupPreviewCase(
                options = listOf(
                    FilterToggleOption("Sim", "Sim", contentDescription = "B3 informado: Sim"),
                ),
                initialSelected = setOf(),
            ),
        )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(name = "Light", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun FilterToggleGroupPreview(
    @PreviewParameter(FilterToggleGroupPreviewProvider::class) previewCase: FilterToggleGroupPreviewCase,
) {

    AppThemeV2 {

        Surface {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterToggleSize.entries.forEach {

                    var selected by remember(previewCase) { mutableStateOf(previewCase.initialSelected) }

                    FilterToggleGroup(
                        options = previewCase.options,
                        selectedIds = selected,
                        onToggle = { id ->
                            selected = if (id in selected) selected - id else selected + id
                        },
                        size = it,
                    )
                }
            }
        }
    }
}