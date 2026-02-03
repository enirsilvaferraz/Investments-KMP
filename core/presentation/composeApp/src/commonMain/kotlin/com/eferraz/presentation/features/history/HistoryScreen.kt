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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.presentation.FixedIncomeHistoryRouting
import com.eferraz.presentation.FundsHistoryRouting
import com.eferraz.presentation.VariableIncomeHistoryRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.design_system.components.new_table.UiTable
import com.eferraz.presentation.features.history.HistoryViewModel.HistoryIntent
import com.eferraz.presentation.features.history.HistoryViewModel.HistoryState
import com.eferraz.presentation.features.transactions.TransactionPanel
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.InvestmentFundHistoryTableData
import com.eferraz.usecases.entities.VariableIncomeHistoryTableData
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HistoryRoute() {

    val sharedVm = koinViewModel<HistoryViewModel>()
    val sharedState by sharedVm.state.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(config, FixedIncomeHistoryRouting)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {

            entry<FixedIncomeHistoryRouting> {
                HistoryScreen(sharedState, sharedVm, backStack)
            }

            entry<VariableIncomeHistoryRouting> {
                HistoryScreen(sharedState, sharedVm, backStack)
            }

            entry<FundsHistoryRouting> {
                HistoryScreen(sharedState, sharedVm, backStack)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun HistoryScreen(
    sharedState: HistoryState,
    sharedVm: HistoryViewModel,
    backStack: NavBackStack<NavKey>,
) {
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
                        selected = sharedState.selectedPeriod,
                        periods = sharedState.periods,
                        onSelect = { sharedVm.processIntent(HistoryIntent.SelectPeriod(it)) }
                    )
                    SyncButton(
                        onClick = { sharedVm.processIntent(HistoryIntent.Sync) }
                    )
                }
            }
        },
        mainPane = {

            Box(modifier = Modifier.fillMaxSize()) {

                when (sharedState.currentCategory) {

                    InvestmentCategory.FIXED_INCOME -> HistoryScreenFixedIncome(
                        state = sharedState,
                        viewModel = sharedVm,
                        scope = scope,
                        navigator = navigator
                    )

                    InvestmentCategory.VARIABLE_INCOME -> HistoryScreenVariableIncome(
                        state = sharedState,
                        viewModel = sharedVm,
                        scope = scope,
                        navigator = navigator
                    )

                    InvestmentCategory.INVESTMENT_FUND -> HistoryScreenFunds(
                        state = sharedState,
                        viewModel = sharedVm,
                        scope = scope,
                        navigator = navigator
                    )
                }

                SegmentedControl(
                    options = listOf(

                        SegmentedOption(
                            value = InvestmentCategory.FIXED_INCOME,
                            label = "Renda Fixa",
                            icon = Icons.Default.Savings,
                            contentDescription = "Renda Fixa"
                        ),

                        SegmentedOption(
                            value = InvestmentCategory.VARIABLE_INCOME,
                            label = "Renda Variável",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Renda Variável"
                        ),

                        SegmentedOption(
                            value = InvestmentCategory.INVESTMENT_FUND,
                            label = "Fundos",
                            icon = Icons.Default.AccountBalance,
                            contentDescription = "Fundos"
                        )
                    ),
                    selectedValue = sharedState.currentCategory,
                    onValueChange = { sharedVm.processIntent(HistoryIntent.SelectCategory(it)) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp).align(Alignment.BottomStart)
                )
            }
        },

        extraPane = {
            if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                TransactionPanel(
                    selectedHolding = sharedState.selectedHolding
                )
            }
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun HistoryScreenFixedIncome(
    modifier: Modifier = Modifier,
    state: HistoryState,
    viewModel: HistoryViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    navigator: androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator<Nothing>,
) {
    val fixedIncomeData = state.tableData.filterIsInstance<FixedIncomeHistoryTableData>()

    UiTable(
        modifier = modifier,
        data = fixedIncomeData,
        groupBy = { it.brokerageName },
        groupDisplay = { it.toString() },
        onSelect = { row ->
            scope.launch {
                viewModel.processIntent(HistoryIntent.SelectHolding(row.currentEntry.holding))
                navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
            }
        }
    ) {

//        column(
//            header = "Corretora",
//            sortedBy = { it.brokerageName },
//            weight = 1.1f,
//            cellValue = { it.brokerageName }
//        )

        column(
            header = "SubCategoria",
            sortedBy = { it.subType },
            weight = 1.0f,
            cellValue = { it.subType.formated() }
        )

        column(
            header = "Display Name",
            sortedBy = { it.displayName },
            weight = 3.0f,
            cellValue = { it.displayName }
        )

//        column(
//            header = "Tipo",
//            sortedBy = { it.type },
//            weight = 1.0f,
//            cellValue = { it.type.formated() }
//        )
//
//        column(
//            header = "Vencimento",
//            alignment = Alignment.CenterHorizontally,
//            sortedBy = { it.expirationDate.toString() },
//            weight = 1.0f,
//            cellValue = { it.expirationDate.formated() }
//        )
//
//        column(
//            header = "Taxa",
//            sortedBy = { it.contractedYield },
//            weight = 0.9f,
//            cellValue = { it.contractedYield.toPercentage() }
//        )

//        column(
//            header = "% CDI",
//            sortedBy = { it.cdiRelativeYield ?: 0.0 },
//            weight = 0.9f,
//            cellValue = { it.cdiRelativeYield?.toString() ?: "" }
//        )

        column(
            header = "Emissor",
            sortedBy = { it.issuerName },
            weight = 1.3f,
            cellValue = { it.issuerName }
        )

        column(
            header = "Liquidez",
            sortedBy = { it.liquidity },
            weight = 1.1f,
            cellValue = { it.liquidity.formated() }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            weight = 1.8f,
            cellValue = { it.observations }
        )

        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.previousValue },
            weight = 1.4f,
            cellValue = { it.previousValue.currencyFormat() },
            footer = { data -> data.sumOf { it.previousValue }.currencyFormat() }
        )

        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.currentValue,
                    onValueChange = { value ->
                        viewModel.processIntent(HistoryIntent.UpdateEntryValue(row.currentEntry, value ?: 0.0))
                    },
                    enabled = row.editable
                )
            },
            footer = { data -> data.sumOf { it.currentValue }.currencyFormat() }
        )

        column(
            header = "Aportes",
            alignment = Alignment.End,
            sortedBy = { it.totalContributions },
            weight = 1.3f,
            cellValue = { it.totalContributions.currencyFormat() },
            footer = { data -> data.sumOf { it.totalContributions }.currencyFormat() }
        )

        column(
            header = "Retiradas",
            alignment = Alignment.End,
            sortedBy = { it.totalWithdrawals },
            weight = 1.3f,
            cellValue = { it.totalWithdrawals.currencyFormat() },
            footer = { data -> data.sumOf { it.totalWithdrawals }.currencyFormat() }
        )

        column(
            header = "%",
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.appreciation },
            weight = 0.7f,
            cellValue = { it.appreciation.toPercentage() },
            footer = { data ->
                val vf = data.sumOf { it.currentValue }
                val vi = data.sumOf { it.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun HistoryScreenVariableIncome(
    modifier: Modifier = Modifier,
    state: HistoryState,
    viewModel: HistoryViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    navigator: androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator<Nothing>,
) {
    val variableIncomeData = state.tableData.filterIsInstance<VariableIncomeHistoryTableData>()

    UiTable(
        modifier = modifier,
        data = variableIncomeData,
        groupBy = { it.brokerageName },
        groupDisplay = { it.toString() },
        onSelect = { row ->
            scope.launch {
                viewModel.processIntent(HistoryIntent.SelectHolding(row.currentEntry.holding))
                navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
            }
        }
    ) {

//        column(
//            header = "Corretora",
//            sortedBy = { it.brokerageName },
//            weight = 1.1f,
//            cellValue = { it.brokerageName }
//        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            weight = 1.0f,
            cellValue = { it.type.formated() }
        )

//        column(
//            header = "Display Name",
//            sortedBy = { it.displayName },
//            weight = 3.0f,
//            cellValue = { it.displayName }
//        )

        column(
            header = "Ticker",
            sortedBy = { it.ticker },
            weight = 0.8f,
            cellValue = { it.ticker }
        )

        column(
            header = "CNPJ",
            sortedBy = { it.cnpj },
            weight = 0.9f,
            cellValue = { it.cnpj }
        )

        column(
            header = "Nome",
            sortedBy = { it.name },
            weight = 2.2f,
            cellValue = { it.name }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            weight = 1.0f,
            cellValue = { it.observations }
        )

        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.previousValue },
            weight = 1.4f,
            cellValue = { it.previousValue.currencyFormat() },
            footer = { data -> data.sumOf { it.previousValue }.currencyFormat() }
        )

        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.currentValue,
                    onValueChange = { value ->
                        viewModel.processIntent(HistoryIntent.UpdateEntryValue(row.currentEntry, value ?: 0.0))
                    },
                    enabled = row.editable
                )
            },
            footer = { data -> data.sumOf { it.currentValue }.currencyFormat() }
        )

        column(
            header = "Aportes",
            alignment = Alignment.End,
            sortedBy = { it.totalContributions },
            weight = 1.3f,
            cellValue = { it.totalContributions.currencyFormat() },
            footer = { data -> data.sumOf { it.totalContributions }.currencyFormat() }
        )

        column(
            header = "Retiradas",
            alignment = Alignment.End,
            sortedBy = { it.totalWithdrawals },
            weight = 1.3f,
            cellValue = { it.totalWithdrawals.currencyFormat() },
            footer = { data -> data.sumOf { it.totalWithdrawals }.currencyFormat() }
        )

        column(
            header = "%",
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.appreciation },
            weight = 0.7f,
            cellValue = { it.appreciation.toPercentage() },
            footer = { data ->
                val vf = data.sumOf { it.currentValue }
                val vi = data.sumOf { it.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun HistoryScreenFunds(
    modifier: Modifier = Modifier,
    state: HistoryState,
    viewModel: HistoryViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    navigator: androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator<Nothing>,
) {
    val fundsData = state.tableData.filterIsInstance<InvestmentFundHistoryTableData>()

    UiTable(
        modifier = modifier,
        data = fundsData,
        groupBy = { it.brokerageName },
        groupDisplay = { it.toString() },
        onSelect = { row ->
            scope.launch {
                viewModel.processIntent(HistoryIntent.SelectHolding(row.currentEntry.holding))
                navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
            }
        }
    ) {

//        column(
//            header = "Corretora",
//            sortedBy = { it.brokerageName },
//            weight = 1.1f,
//            cellValue = { it.brokerageName }
//        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            weight = 1.0f,
            cellValue = { it.type.formated() }
        )

//        column(
//            header = "Display Name",
//            sortedBy = { it.displayName },
//            weight = 3.0f,
//            cellValue = { it.displayName }
//        )

        column(
            header = "Nome",
            sortedBy = { it.name },
            weight = 2.1f,
            cellValue = { it.name }
        )

        column(
            header = "Liquidez",
            sortedBy = { it.liquidity },
            weight = 1.1f,
            cellValue = { it.liquidity.formated(it.liquidityDays) }
        )

        column(
            header = "Dias Liq.",
            sortedBy = { it.liquidityDays },
            weight = 0.9f,
            cellValue = { it.liquidityDays.toString() }
        )

        column(
            header = "Vencimento",
            sortedBy = { it.expirationDate?.toString() ?: "" },
            weight = 1.0f,
            cellValue = { it.expirationDate?.formated() ?: "" }
        )

        column(
            header = "Emissor",
            sortedBy = { it.issuerName },
            weight = 1.3f,
            cellValue = { it.issuerName }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            weight = 1.0f,
            cellValue = { it.observations }
        )

        column(
            header = "Valor Anterior",
            alignment = Alignment.End,
            sortedBy = { it.previousValue },
            weight = 1.4f,
            cellValue = { it.previousValue.currencyFormat() },
            footer = { data -> data.sumOf { it.previousValue }.currencyFormat() }
        )

        column(
            header = "Valor Atual",
            alignment = Alignment.End,
            sortedBy = { it.currentValue },
            weight = 1.4f,
            cellContent = { row ->
                TableInputMoney(
                    value = row.currentValue,
                    onValueChange = { value ->
                        viewModel.processIntent(HistoryIntent.UpdateEntryValue(row.currentEntry, value ?: 0.0))
                    },
                    enabled = row.editable
                )
            },
            footer = { data -> data.sumOf { it.currentValue }.currencyFormat() }
        )

        column(
            header = "Aportes",
            alignment = Alignment.End,
            sortedBy = { it.totalContributions },
            weight = 1.3f,
            cellValue = { it.totalContributions.currencyFormat() },
            footer = { data -> data.sumOf { it.totalContributions }.currencyFormat() }
        )

        column(
            header = "Retiradas",
            alignment = Alignment.End,
            sortedBy = { it.totalWithdrawals },
            weight = 1.3f,
            cellValue = { it.totalWithdrawals.currencyFormat() },
            footer = { data -> data.sumOf { it.totalWithdrawals }.currencyFormat() }
        )

        column(
            header = "%",
            weight = 0.7f,
            alignment = Alignment.CenterHorizontally,
            sortedBy = { it.appreciation },
            cellValue = { it.appreciation.toPercentage() },
            footer = { data ->
                val vf = data.sumOf { it.currentValue }
                val vi = data.sumOf { it.previousValue }
                if (vi > 0) ((vf - vi) / vi * 100).toPercentage() else "—"
            }
        )
    }
}

