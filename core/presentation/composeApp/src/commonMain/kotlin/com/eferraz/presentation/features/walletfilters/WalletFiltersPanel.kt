@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.eferraz.presentation.features.walletfilters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.filter.FilterSectionHeader
import com.eferraz.design_system_v2.filter.FilterToggleGroup
import com.eferraz.design_system_v2.filter.FilterToggleOption
import com.eferraz.design_system_v2.filter.FilterToggleSize
import com.eferraz.design_system_v2.filter.MaturityFilterDropdown
import com.eferraz.design_system_v2.theme.AppThemeV2
import kotlinx.datetime.YearMonth

@Composable
internal fun WalletFiltersPanel(
    options: WalletFiltersPanelOptions,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        PanelHeader(
            title = "Filtros",
            onReset = { onStateChange(state.reset()) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            SectionCommon(
                options = options,
                state = state,
                onStateChange = onStateChange
            )

            options.subtypeSections.forEach { section ->

                FilterSection {

                    FilterSectionHeader(
                        icon = Icons.Outlined.FilterList,
                        label = section.title
                    )

                    FilterToggleGroup(
                        section.options.toToggleOptions(),
                        state.selectedSubtypeIds,
                        { onStateChange(state.toggleSubtype(it)) },
                        size = FilterToggleSize.Standard
                    )

                    if (section.title == "Renda Fixa") {

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                            if (options.liquidityOptions.isNotEmpty()) {

                                ToggleSection(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.CalendarMonth,
                                    label = "Liquidez",
                                    options = options.liquidityOptions,
                                    selectedIds = state.selectedLiquidityIds,
                                    onToggle = { onStateChange(state.toggleLiquidity(it)) },
                                )
                            }

                            if (options.maturityMonths.isNotEmpty()) {

                                MaturityFilterSection(
                                    modifier = Modifier.weight(1f),
                                    selection = state.maturitySelection,
                                    months = options.maturityMonths,
                                    onSelectMonth = { onStateChange(state.selectMaturity(it)) },
                                    sectionIcon = Icons.Outlined.CalendarMonth,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCommon(
    options: WalletFiltersPanelOptions,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
) {
    FilterSection {

        ToggleSection(
            icon = Icons.Outlined.Layers,
            label = "Classe",
            options = options.classOptions,
            selectedIds = state.selectedClassIds,
            onToggle = { onStateChange(state.toggleClass(it)) },
            size = FilterToggleSize.Standard
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ToggleSection(
                icon = Icons.Outlined.Info,
                label = "B3 informado",
                options = options.b3Options,
                selectedIds = state.selectedB3Ids,
                onToggle = { onStateChange(state.toggleB3(it)) },
                modifier = Modifier.weight(1f),
            )

            ToggleSection(
                icon = Icons.Outlined.Sync,
                label = "Liquidados",
                options = options.settledOptions,
                selectedIds = state.selectedSettledIds,
                onToggle = { onStateChange(state.toggleSettled(it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}


@Composable
private fun PanelHeader(
    title: String,
    onReset: (() -> Unit)?,
) {

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        Icon(
            imageVector = Icons.Outlined.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )

        onReset?.let {
            TextButton(onClick = onReset) {
                Text("Resetar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FilterSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {

    OutlinedCard {

        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun MaturityFilterSection(
    selection: YearMonth?,
    months: List<YearMonth>,
    onSelectMonth: (YearMonth?) -> Unit,
    modifier: Modifier = Modifier,
    sectionIcon: ImageVector = Icons.Outlined.CalendarMonth,
) {

    if (months.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FilterSectionHeader(
            icon = sectionIcon, label = "Vence até"
        )

        MaturityFilterDropdown(
            modifier = Modifier.padding(top = 3.dp),
            selection = selection,
            months = months,
            onSelectMonth = onSelectMonth,
        )
    }
}

@Composable
private fun ToggleSection(
    icon: ImageVector,
    label: String,
    options: List<FilterOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    size: FilterToggleSize = FilterToggleSize.Standard,
    modifier: Modifier = Modifier,
) {

    if (options.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FilterSectionHeader(
            icon = icon,
            label = label
        )

        FilterToggleGroup(
            options = options.toToggleOptions(),
            selectedIds = selectedIds,
            onToggle = onToggle,
            size = size
        )
    }
}

private fun List<FilterOption>.toToggleOptions() = map { FilterToggleOption(it.id, it.shortLabel, it.fullLabel) }

private class WalletFiltersPanelPreviewProvider : PreviewParameterProvider<WalletFiltersPanelOptions> {
    override val values: Sequence<WalletFiltersPanelOptions> = sequenceOf(WalletFiltersPreviewCatalog.fullPanelOptions)
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun WalletFiltersPanelPreviewEdgeCases(
    @PreviewParameter(WalletFiltersPanelPreviewProvider::class) case: WalletFiltersPanelOptions,
) {

    var state by remember { mutableStateOf(WalletFiltersUiState.initial()) }

    AppThemeV2 {
        Surface {
            WalletFiltersPanel(
                options = case,
                state = state,
                onStateChange = { state = it },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
