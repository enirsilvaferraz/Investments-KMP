@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.eferraz.presentation.features.walletfilters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.eferraz.design_system_v2.filter.MaturityFilterDropdown
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.entities.assets.InvestmentCategory
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        PanelHeader(
            title = "Filtros",
            onReset = { onStateChange(state.reset()) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            ComunsSection(
                section = options.commons,
                state = state,
                onStateChange = onStateChange,
            )

            if (state.isClassSelected(InvestmentCategory.FIXED_INCOME)) {
                RendaFixaSection(
                    section = options.fixedIncome,
                    state = state,
                    onStateChange = onStateChange,
                )
            }

            if (state.isClassSelected(InvestmentCategory.VARIABLE_INCOME)) {
                RendaVariavelSection(
                    section = options.variableIncome,
                    selectedSubtypeIds = state.selectedSubtypeIds,
                    onToggleSubtype = { onStateChange(state.toggleSubtype(it)) },
                )
            }

            if (state.isClassSelected(InvestmentCategory.INVESTMENT_FUND)) {
                FundosSection(
                    section = options.funds,
                    selectedSubtypeIds = state.selectedSubtypeIds,
                    onToggleSubtype = { onStateChange(state.toggleSubtype(it)) },
                )
            }
        }
    }
}

@Composable
private fun ComunsSection(
    section: WalletFiltersComunsSectionOptions,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
) {

    FilterSection {

        ToggleSection(
            icon = WalletFilterSectionIcons.assetClass,
            label = "Classe",
            options = section.classOptions,
            selectedIds = state.selectedClassIds,
            onToggle = { onStateChange(state.toggleClass(it)) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ToggleSection(
                icon = WalletFilterSectionIcons.b3Informed,
                label = "B3 informado",
                options = section.b3Options,
                selectedIds = state.selectedB3Ids,
                onToggle = { onStateChange(state.toggleB3(it)) },
                modifier = Modifier.weight(1f),
            )

            ToggleSection(
                icon = WalletFilterSectionIcons.settled,
                label = "Liquidados",
                options = section.settledOptions,
                selectedIds = state.selectedSettledIds,
                onToggle = { onStateChange(state.toggleSettled(it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RendaFixaSection(
    section: WalletFiltersFixedIncomeSectionOptions,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
) {

    FilterSection {

        SubtypeToggleBlock(
            title = "Renda Fixa",
            subtypeOptions = section.subtypeOptions,
            selectedSubtypeIds = state.selectedSubtypeIds,
            onToggleSubtype = { onStateChange(state.toggleSubtype(it)) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ToggleSection(
                modifier = Modifier.weight(1f),
                icon = WalletFilterSectionIcons.liquidity,
                label = "Liquidez",
                options = section.liquidityOptions,
                selectedIds = state.selectedLiquidityIds,
                onToggle = { onStateChange(state.toggleLiquidity(it)) },
            )

            MaturityFilterSection(
                modifier = Modifier.weight(1f),
                selection = state.maturitySelection,
                months = section.maturityMonths,
                onSelectMonth = { onStateChange(state.selectMaturity(it)) },
                sectionIcon = WalletFilterSectionIcons.maturity,
            )
        }
    }
}

@Composable
private fun RendaVariavelSection(
    section: WalletFiltersVariableIncomeSectionOptions,
    selectedSubtypeIds: Set<String>,
    onToggleSubtype: (String) -> Unit,
) {
    FilterSection {
        SubtypeToggleBlock(
            title = "Renda Variável",
            subtypeOptions = section.subtypeOptions,
            selectedSubtypeIds = selectedSubtypeIds,
            onToggleSubtype = onToggleSubtype,
        )
    }
}

@Composable
private fun FundosSection(
    section: WalletFiltersFundsSectionOptions,
    selectedSubtypeIds: Set<String>,
    onToggleSubtype: (String) -> Unit,
) {
    FilterSection {
        SubtypeToggleBlock(
            title = "Fundos",
            subtypeOptions = section.subtypeOptions,
            selectedSubtypeIds = selectedSubtypeIds,
            onToggleSubtype = onToggleSubtype,
        )
    }
}

@Composable
private fun SubtypeToggleBlock(
    title: String,
    subtypeOptions: List<FilterOption>,
    selectedSubtypeIds: Set<String>,
    onToggleSubtype: (String) -> Unit,
) {

    FilterSectionHeader(
        icon = WalletFilterSectionIcons.subtype,
        label = title,
    )

    FilterToggleGroup(
        subtypeOptions.toToggleOptions(),
        selectedSubtypeIds,
        onToggleSubtype,
    )
}

@Composable
private fun PanelHeader(
    title: String,
    onReset: (() -> Unit)?,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = WalletFilterSectionIcons.panelHeader,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
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
            content = content,
        )
    }
}

@Composable
private fun MaturityFilterSection(
    selection: YearMonth?,
    months: List<YearMonth>,
    onSelectMonth: (YearMonth?) -> Unit,
    modifier: Modifier = Modifier,
    sectionIcon: ImageVector = WalletFilterSectionIcons.maturity,
) {
    if (months.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterSectionHeader(
            icon = sectionIcon,
            label = "Vence até",
        )

        MaturityFilterDropdown(
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
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterSectionHeader(
            icon = icon,
            label = label,
        )

        FilterToggleGroup(
            options = options.toToggleOptions(),
            selectedIds = selectedIds,
            onToggle = onToggle,
        )
    }
}

private fun List<FilterOption>.toToggleOptions() =
    map { FilterToggleOption(it.id, it.shortLabel, it.fullLabel) }

private class WalletFiltersPanelPreviewProvider : PreviewParameterProvider<WalletFiltersPanelOptions> {
    override val values: Sequence<WalletFiltersPanelOptions> =
        sequenceOf(WalletFiltersPreviewCatalog.fullPanelOptions)
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
