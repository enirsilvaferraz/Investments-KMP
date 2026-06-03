@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.eferraz.presentation.features.walletfilters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.eferraz.design_system_v2.filter.MaturityFilterDropdown
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.entities.assets.AssetClass

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
            imageVector = WalletFilterSectionIcons.panelHeader,
            title = "Filtros",
            onReset = { onStateChange(state.reset()) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            ComunsSection(
                section = options.commons,
                state = state,
                onStateChange = onStateChange,
            )

            if (state.isClassSelected(AssetClass.FIXED_INCOME)) {
                RendaFixaSection(
                    section = options.fixedIncome,
                    state = state,
                    onStateChange = onStateChange,
                )
            }

            if (state.isClassSelected(AssetClass.VARIABLE_INCOME)) {
                RendaVariavelSection(
                    section = options.variableIncome,
                    selectedSubtypes = state.selectedSubtypes,
                    onToggleSubtype = { onStateChange(state.toggleSubtype(it)) },
                )
            }

            if (state.isClassSelected(AssetClass.INVESTMENT_FUND)) {
                FundosSection(
                    section = options.funds,
                    selectedSubtypes = state.selectedSubtypes,
                    onToggleSubtype = { onStateChange(state.toggleSubtype(it)) },
                )
            }
        }
    }
}

@Composable
private fun ComunsSection(
    section: WalletFiltersPanelOptions.Commons,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
) {

    PanelCard {

        PanelLabel(
            icon = WalletFilterSectionIcons.assetClass,
            label = "Classe",
        ) {

            FilterToggleGroup(
                options = section.classOptions.toToggleOptions(),
                selectedIds = state.selectedCategories,
                onToggle = { onStateChange(state.toggleClass(it)) },
            )
        }

        PanelLabel(
            icon = WalletFilterSectionIcons.b3Informed,
            label = "B3 Code",
        ) {

            FilterToggleGroup(
                options = section.b3Options.toToggleOptions(),
                selectedIds = state.selectedB3,
                onToggle = { onStateChange(state.toggleB3(it)) },
            )
        }

        PanelLabel(
            icon = WalletFilterSectionIcons.settled,
            label = "Liquidados",
        ) {

            FilterToggleGroup(
                options = section.settledOptions.toToggleOptions(),
                selectedIds = state.selectedSettled,
                onToggle = { onStateChange(state.toggleSettled(it)) },
            )
        }
    }
}

@Composable
private fun RendaFixaSection(
    section: WalletFiltersPanelOptions.FixedIncome,
    state: WalletFiltersUiState,
    onStateChange: (WalletFiltersUiState) -> Unit,
) {

    PanelCard {

        PanelLabel(
            label = "R. Fixa",
            icon = WalletFilterSectionIcons.subtype
        ) {

            FilterToggleGroup(
                section.subtypeOptions.toToggleOptions(),
                state.selectedSubtypes,
                { onStateChange(state.toggleSubtype(it)) }
            )
        }

        PanelLabel(
            icon = WalletFilterSectionIcons.liquidity,
            label = "Liquidez",
        ) {

            FilterToggleGroup(
                options = section.liquidityOptions.toToggleOptions(),
                selectedIds = state.selectedLiquidities,
                onToggle = { onStateChange(state.toggleLiquidity(it)) },
            )
        }

        PanelLabel(
            icon = WalletFilterSectionIcons.maturity,
            label = "Vence até",
        ) {

            MaturityFilterDropdown(
                selection = state.maturitySelection,
                months = section.maturityMonths,
                onSelectMonth = { month ->
                    onStateChange(if (month == null) state.selectMaturityAny() else state.selectMaturity(month))
                }
            )
        }
    }
}

@Composable
private fun RendaVariavelSection(
    section: WalletFiltersPanelOptions.VariableIncome,
    selectedSubtypes: Set<WalletFilterSubtype>,
    onToggleSubtype: (WalletFilterSubtype) -> Unit,
) {
    PanelCard {

        PanelLabel(
            label = "R. Variável",
            icon = WalletFilterSectionIcons.subtype
        ) {

            FilterToggleGroup(
                section.subtypeOptions.toToggleOptions(),
                selectedSubtypes,
                onToggleSubtype,
            )
        }
    }
}

@Composable
private fun FundosSection(
    section: WalletFiltersPanelOptions.Funds,
    selectedSubtypes: Set<WalletFilterSubtype>,
    onToggleSubtype: (WalletFilterSubtype) -> Unit,
) {
    PanelCard {

        PanelLabel(
            label = "Fundos",
            icon = WalletFilterSectionIcons.subtype
        ) {

            FilterToggleGroup(
                section.subtypeOptions.toToggleOptions(),
                selectedSubtypes,
                onToggleSubtype,
            )
        }
    }
}

@Composable
internal fun PanelHeader(
    imageVector: ImageVector?,
    title: String,
    onReset: (() -> Unit)?,
) {

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        imageVector?.let {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
private fun PanelLabel(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FilterSectionHeader(
            modifier = Modifier.width(120.dp),
            icon = icon,
            label = label,
        )

        content()
    }
}

@Composable
private fun PanelCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth(),
            content = content,
        )
    }
}

private class WalletFiltersPanelPreviewProvider : PreviewParameterProvider<WalletFiltersPanelOptions> {
    override val values: Sequence<WalletFiltersPanelOptions> =
        sequenceOf(WalletFiltersPreviewCatalog.fullPanelOptions)
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO, widthDp = 500)
@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES, widthDp = 500)
@Composable
private fun WalletFiltersPanelPreviewEdgeCases(
    @PreviewParameter(WalletFiltersPanelPreviewProvider::class) case: WalletFiltersPanelOptions,
) {
    var state by remember {
        mutableStateOf(
            WalletFiltersUiState(
                selectedCategories = AssetClass.entries.toSet(),
            )
        )
    }

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
