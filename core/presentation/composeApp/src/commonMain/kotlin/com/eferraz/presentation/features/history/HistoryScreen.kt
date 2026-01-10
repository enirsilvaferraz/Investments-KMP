package com.eferraz.presentation.features.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.FixedIncomeHistoryRouting
import com.eferraz.presentation.FundsHistoryRouting
import com.eferraz.presentation.VariableIncomeHistoryRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.design_system.components.new_table.UiTable
import androidx.compose.ui.text.style.TextAlign
import com.eferraz.presentation.features.transactions.TransactionPanel
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.HoldingHistoryResult
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Posicionamento no Período",
        navigator = navigator,
        actions = {
            Row {
                if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                    FilledTonalIconButton(
                        onClick = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                } else {
                    PeriodActions(
                        selected = state.selectedPeriod,
                        periods = state.periods,
                        onSelect = { vm.processIntent(HistoryIntent.SelectPeriod(it)) }
                    )
                    SyncButton(
                        onClick = { vm.processIntent(HistoryIntent.Sync) }
                    )
                }
            }
        },
        mainPane = {
            val backStack = rememberNavBackStack(config, FixedIncomeHistoryRouting)

            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<FixedIncomeHistoryRouting> {
                            HistoryScreenFixedIncome(
                                entries = state.entries.filter { it.holding.asset is FixedIncomeAsset },
                                onUpdateValue = { entry, value ->
                                    vm.processIntent(HistoryIntent.UpdateEntryValue(entry, value))
                                },
                                onRowClick = { row ->
                                    scope.launch {
                                        vm.processIntent(HistoryIntent.SelectHolding(row.currentHistory.holding))
                                        navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                    }
                                }
                            )
                        }

                        entry<VariableIncomeHistoryRouting> {
                            HistoryScreenVariableIncome(
                                entries = state.entries.filter { it.holding.asset is VariableIncomeAsset },
                                onUpdateValue = { entry, value ->
                                    vm.processIntent(HistoryIntent.UpdateEntryValue(entry, value))
                                },
                                onRowClick = { row ->
                                    scope.launch {
                                        vm.processIntent(HistoryIntent.SelectHolding(row.currentHistory.holding))
                                        navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                    }
                                }
                            )
                        }

                        entry<FundsHistoryRouting> {
                            HistoryScreenFunds(
                                entries = state.entries.filter { it.holding.asset is InvestmentFundAsset },
                                onUpdateValue = { entry, value ->
                                    vm.processIntent(HistoryIntent.UpdateEntryValue(entry, value))
                                },
                                onRowClick = { row ->
                                    scope.launch {
                                        vm.processIntent(HistoryIntent.SelectHolding(row.currentHistory.holding))
                                        navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                    }
                                }
                            )
                        }
                    }
                )

                SegmentedControl(
                    options = listOf(
                        SegmentedOption(
                            value = FixedIncomeHistoryRouting,
                            label = "Renda Fixa",
                            icon = Icons.Default.Savings,
                            contentDescription = "Renda Fixa"
                        ),
                        SegmentedOption(
                            value = VariableIncomeHistoryRouting,
                            label = "Renda Variável",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Renda Variável"
                        ),
                        SegmentedOption(
                            value = FundsHistoryRouting,
                            label = "Fundos",
                            icon = Icons.Default.AccountBalance,
                            contentDescription = "Fundos"
                        )
                    ),
                    selectedValue = backStack.lastOrNull() ?: FixedIncomeHistoryRouting,
                    onValueChange = { backStack[0] = it },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp).align(Alignment.BottomStart)
                )
            }
        },
        extraPane = {
            if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
                TransactionPanel(
                    selectedHolding = state.selectedHolding
                )
        }
    )
}

@Composable
private fun PeriodActions(
    modifier: Modifier = Modifier,
    selected: YearMonth,
    periods: List<YearMonth>,
    onSelect: (YearMonth) -> Unit,
) {

    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {

        items(periods) { current ->

            AnimatedVisibility(
                visible = selected == current || expanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        expanded = !expanded
                        if (selected != current) onSelect(current)
                    },
                    label = { Text(current.formated()) },
                    selected = current == selected,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (current == selected) colors.primaryContainer else colors.surface,
                        selectedContainerColor = colors.primaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun SyncButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {

    FilledTonalIconButton(
        onClick = onClick
    ) {
        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
    }
}

@Composable
private fun HistoryScreenFixedIncome(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
    onRowClick: (HoldingHistoryRow) -> Unit,
) {
    val data = entries.map { result ->
        HoldingHistoryRow.create(
            result.holding,
            result.currentEntry,
            result.previousEntry,
            result.profitOrLoss
        )
    }
    
    UiTable(
        modifier = modifier,
        data = data,
        onSelect = onRowClick
    ) {
        column(
            header = "Corretora",
            sortedBy = { it.viewData.brokerage },
            weight = 1.1f,
            cellValue = { it.viewData.brokerage }
        )
        
        column(
            header = "SubCategoria",
            sortedBy = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.subType?.name ?: "" },
            weight = 1.0f,
            cellValue = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.subType?.formated() ?: "" }
        )
        
        column(
            header = "Tipo",
            sortedBy = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.type?.name ?: "" },
            weight = 1.0f,
            cellValue = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.type?.formated() ?: "" }
        )
        
        column(
            header = "Vencimento",
            sortedBy = { it.viewData.maturity?.toString() ?: "" },
            weight = 1.0f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.maturity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        )
        
        column(
            header = "Taxa",
            sortedBy = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.contractedYield ?: 0.0 },
            weight = 0.9f,
            cellValue = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.contractedYield?.toString() ?: "" }
        )
        
        column(
            header = "% CDI",
            sortedBy = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.cdiRelativeYield ?: 0.0 },
            weight = 0.9f,
            cellValue = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.cdiRelativeYield?.toString() ?: "" }
        )
        
        column(
            header = "Emissor",
            sortedBy = { it.viewData.issuer },
            weight = 1.3f,
            cellValue = { it.viewData.issuer }
        )
        
        column(
            header = "Liquidez",
            sortedBy = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.liquidity?.name ?: "" },
            weight = 1.1f,
            cellValue = { (it.currentHistory.holding.asset as? FixedIncomeAsset)?.liquidity?.formated() ?: "" }
        )
        
        column(
            header = "Observação",
            sortedBy = { it.viewData.observations.orEmpty() },
            weight = 1.8f,
            cellValue = { it.viewData.observations.orEmpty() }
        )
        
        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.viewData.previousValue },
            weight = 1.4f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.previousValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            },
            footer = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
        )
        
        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.viewData.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.viewData.currentValue,
                    onValueChange = { value -> onUpdateValue(row.currentHistory, value ?: 0.0) },
                    enabled = row.viewData.editable
                )
            },
            footer = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
        )
        
        column(
            header = "%",
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.viewData.appreciation },
            weight = 0.7f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.appreciation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            footer = { data ->
                val vf = data.sumOf { it.viewData.currentValue }
                val vi = data.sumOf { it.viewData.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

@Composable
private fun HistoryScreenVariableIncome(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
    onRowClick: (HoldingHistoryRow) -> Unit,
) {
    val data = entries.map { result ->
        HoldingHistoryRow.create(
            result.holding,
            result.currentEntry,
            result.previousEntry,
            result.profitOrLoss
        )
    }
    
    UiTable(
        modifier = modifier,
        data = data,
        onSelect = onRowClick
    ) {
        column(
            header = "Corretora",
            sortedBy = { it.viewData.brokerage },
            weight = 1.1f,
            cellValue = { it.viewData.brokerage }
        )
        
        column(
            header = "Tipo",
            sortedBy = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.type?.name ?: "" },
            weight = 1.0f,
            cellValue = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.type?.formated() ?: "" }
        )
        
        column(
            header = "Ticker",
            sortedBy = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.ticker ?: "" },
            weight = 0.8f,
            cellValue = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.ticker ?: "" }
        )
        
        column(
            header = "CNPJ",
            sortedBy = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.cnpj?.get() ?: "" },
            weight = 0.9f,
            cellValue = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.cnpj?.get() ?: "" }
        )
        
        column(
            header = "Nome",
            sortedBy = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.name ?: "" },
            weight = 2.2f,
            cellValue = { (it.currentHistory.holding.asset as? VariableIncomeAsset)?.name ?: "" }
        )
        
        column(
            header = "Observação",
            sortedBy = { it.viewData.observations.orEmpty() },
            weight = 1.0f,
            cellValue = { it.viewData.observations.orEmpty() }
        )
        
        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.viewData.previousValue },
            weight = 1.4f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.previousValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            },
            footer = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
        )
        
        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.viewData.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.viewData.currentValue,
                    onValueChange = { value -> onUpdateValue(row.currentHistory, value ?: 0.0) },
                    enabled = row.viewData.editable
                )
            },
            footer = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
        )
        
        column(
            header = "%",
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.viewData.appreciation },
            weight = 0.7f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.appreciation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            footer = { data ->
                val vf = data.sumOf { it.viewData.currentValue }
                val vi = data.sumOf { it.viewData.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

@Composable
private fun HistoryScreenFunds(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
    onRowClick: (HoldingHistoryRow) -> Unit,
) {
    val data = entries.map { result ->
        HoldingHistoryRow.create(
            result.holding,
            result.currentEntry,
            result.previousEntry,
            result.profitOrLoss
        )
    }
    
    UiTable(
        modifier = modifier,
        data = data,
        onSelect = onRowClick
    ) {
        column(
            header = "Corretora",
            sortedBy = { it.viewData.brokerage },
            weight = 1.1f,
            cellValue = { it.viewData.brokerage }
        )
        
        column(
            header = "Tipo",
            sortedBy = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.type?.name ?: "" },
            weight = 1.0f,
            cellValue = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.type?.formated() ?: "" }
        )
        
        column(
            header = "Nome",
            sortedBy = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.name ?: "" },
            weight = 2.1f,
            cellValue = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.name ?: "" }
        )
        
        column(
            header = "Liquidez",
            sortedBy = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidity?.name ?: "" },
            weight = 1.1f,
            cellValue = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidity?.formated() ?: "" }
        )
        
        column(
            header = "Dias Liq.",
            sortedBy = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidityDays ?: 0 },
            weight = 0.9f,
            cellValue = { (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidityDays?.toString() ?: "" }
        )
        
        column(
            header = "Vencimento",
            sortedBy = { it.viewData.maturity?.toString() ?: "" },
            weight = 1.0f,
            cellValue = { it.formatted.maturity }
        )
        
        column(
            header = "Emissor",
            sortedBy = { it.viewData.issuer },
            weight = 1.3f,
            cellValue = { it.viewData.issuer }
        )
        
        column(
            header = "Observação",
            sortedBy = { it.viewData.observations },
            weight = 1.0f,
            cellValue = { it.viewData.observations }
        )
        
        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.viewData.previousValue },
            weight = 1.4f,
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.previousValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            },
            footer = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
        )
        
        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.viewData.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.viewData.currentValue,
                    onValueChange = { value -> onUpdateValue(row.currentHistory, value ?: 0.0) },
                    enabled = row.viewData.editable
                )
            },
            footer = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
        )
        
        column(
            header = "%",
            weight = 0.7f,
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.viewData.appreciation },
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.formatted.appreciation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            footer = { data ->
                val vf = data.sumOf { it.viewData.currentValue }
                val vi = data.sumOf { it.viewData.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

