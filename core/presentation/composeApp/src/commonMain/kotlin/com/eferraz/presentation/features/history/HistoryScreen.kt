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
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputMoneyColumn
import com.eferraz.presentation.design_system.components.table.textColumn
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
    DataTable(
        modifier = modifier,
        columns = listOf(
            textColumn(
                title = "Corretora",
                getValue = { it.viewData.brokerage },
                format = { it.viewData.brokerage }
            ),
            textColumn(
                title = "SubCategoria",
                getValue = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.subType?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.subType?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Tipo",
                getValue = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.type?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.type?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Vencimento",
                getValue = { it.viewData.maturity },
                format = { it.formatted.maturity },
                alignment = Alignment.CenterHorizontally
            ),
            textColumn(
                title = "Taxa",
                getValue = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.contractedYield
                },
                format = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.contractedYield?.toString() ?: ""
                }
            ),
            textColumn(
                title = "% CDI",
                getValue = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.cdiRelativeYield
                },
                format = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.cdiRelativeYield?.toString() ?: ""
                }
            ),
            textColumn(
                title = "Emissor",
                getValue = { it.viewData.issuer },
                format = { it.viewData.issuer }
            ),
            textColumn(
                title = "Liquidez",
                getValue = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.liquidity?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? FixedIncomeAsset)?.liquidity?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Observação",
                getValue = { it.viewData.observations },
                format = { it.viewData.observations },
                weight = 2f
            ),
            textColumn(
                title = "Valor Anterior",
                getValue = { it.viewData.previousValue },
                format = { it.formatted.previousValue },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
            ),
            inputMoneyColumn(
                title = "Valor Atual",
                getValue = { it.viewData.currentValue },
                onValueChange = { item, value -> onUpdateValue(item.currentHistory, value ?: 0.0) },
                getEnabled = { it.viewData.editable },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
            ),
            textColumn(
                title = "Valorização",
                getValue = { it.viewData.appreciation },
                format = { it.formatted.appreciation },
                alignment = Alignment.CenterHorizontally,
                footerOperation = { data ->
                    val vf = data.sumOf { it.viewData.currentValue }
                    val vi = data.sumOf { it.viewData.previousValue }
                    if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
                }
            )
        ),
        data = entries.map { result ->
            HoldingHistoryRow.create(
                result.holding,
                result.currentEntry,
                result.previousEntry,
                result.profitOrLoss
            )
        },
        onRowClick = onRowClick,
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Composable
private fun HistoryScreenVariableIncome(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
    onRowClick: (HoldingHistoryRow) -> Unit,
) {
    DataTable(
        modifier = modifier,
        columns = listOf(
            textColumn(
                title = "Corretora",
                getValue = { it.viewData.brokerage },
                format = { it.viewData.brokerage }
            ),
            textColumn(
                title = "Tipo",
                getValue = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.type?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.type?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Ticker",
                getValue = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.ticker ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.ticker ?: ""
                }
            ),
            textColumn(
                title = "CNPJ",
                getValue = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.cnpj?.get() ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.cnpj?.get() ?: ""
                }
            ),
            textColumn(
                title = "Nome",
                getValue = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? VariableIncomeAsset)?.name ?: ""
                },
                weight = 2f
            ),
            textColumn(
                title = "Observação",
                getValue = { it.viewData.observations },
                format = { it.viewData.observations },
                weight = 2f
            ),
            textColumn(
                title = "Valor Anterior",
                getValue = { it.viewData.previousValue },
                format = { it.formatted.previousValue },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
            ),
            inputMoneyColumn(
                title = "Valor Atual",
                getValue = { it.viewData.currentValue },
                onValueChange = { item, value -> onUpdateValue(item.currentHistory, value ?: 0.0) },
                getEnabled = { it.viewData.editable },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
            ),
            textColumn(
                title = "Valorização",
                getValue = { it.viewData.appreciation },
                format = { it.formatted.appreciation },
                alignment = Alignment.CenterHorizontally,
                footerOperation = { data ->
                    val vf = data.sumOf { it.viewData.currentValue }
                    val vi = data.sumOf { it.viewData.previousValue }
                    if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
                }
            )
        ),
        data = entries.map { result ->
            HoldingHistoryRow.create(
                result.holding,
                result.currentEntry,
                result.previousEntry,
                result.profitOrLoss
            )
        },
        onRowClick = onRowClick,
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Composable
private fun HistoryScreenFunds(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
    onRowClick: (HoldingHistoryRow) -> Unit,
) {
    DataTable(
        modifier = modifier,
        columns = listOf(
            textColumn(
                title = "Corretora",
                getValue = { it.viewData.brokerage },
                format = { it.viewData.brokerage }
            ),
            textColumn(
                title = "Tipo",
                getValue = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.type?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.type?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Nome",
                getValue = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.name ?: ""
                },
                weight = 2f
            ),
            textColumn(
                title = "Liquidez",
                getValue = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidity?.name ?: ""
                },
                format = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidity?.formated() ?: ""
                }
            ),
            textColumn(
                title = "Dias Liq.",
                getValue = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidityDays
                },
                format = { 
                    (it.currentHistory.holding.asset as? InvestmentFundAsset)?.liquidityDays?.toString() ?: ""
                }
            ),
            textColumn(
                title = "Vencimento",
                getValue = { it.viewData.maturity },
                format = { it.formatted.maturity }
            ),
            textColumn(
                title = "Emissor",
                getValue = { it.viewData.issuer },
                format = { it.viewData.issuer }
            ),
            textColumn(
                title = "Observação",
                getValue = { it.viewData.observations },
                format = { it.viewData.observations },
                weight = 2f
            ),
            textColumn(
                title = "Valor Anterior",
                getValue = { it.viewData.previousValue },
                format = { it.formatted.previousValue },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.previousValue }.currencyFormat() }
            ),
            inputMoneyColumn(
                title = "Valor Atual",
                getValue = { it.viewData.currentValue },
                onValueChange = { item, value -> onUpdateValue(item.currentHistory, value ?: 0.0) },
                getEnabled = { it.viewData.editable },
                alignment = Alignment.End,
                footerOperation = { data -> data.sumOf { it.viewData.currentValue }.currencyFormat() }
            ),
            textColumn(
                title = "Valorização",
                getValue = { it.viewData.appreciation },
                format = { it.formatted.appreciation },
                alignment = Alignment.CenterHorizontally,
                footerOperation = { data ->
                    val vf = data.sumOf { it.viewData.currentValue }
                    val vi = data.sumOf { it.viewData.previousValue }
                    if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
                }
            )
        ),
        data = entries.map { result ->
            HoldingHistoryRow.create(
                result.holding,
                result.currentEntry,
                result.previousEntry,
                result.profitOrLoss
            )
        },
        onRowClick = onRowClick,
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

