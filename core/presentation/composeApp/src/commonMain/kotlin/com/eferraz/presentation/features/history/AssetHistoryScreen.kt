package com.eferraz.presentation.features.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.design_system.components.segmented_control.SegmentedControl
import com.eferraz.design_system.components.segmented_control.SegmentedControlChoice
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.design_system.scaffolds.AppScreenPane
import com.eferraz.design_system.scaffolds.AppScreenScaffold
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.presentation.commons.table_icons.BuildIcon
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.design_system.theme.getInfoColor
import com.eferraz.presentation.design_system.theme.getSuccessColor
import com.eferraz.presentation.design_system.theme.getWarningColor
import com.eferraz.presentation.features.transactions.TransactionPanel
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.HoldingHistoryView
import com.seanproctor.datatable.TableColumnWidth
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun HoldingHistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    val onValueChange = remember(vm) {
        { entry: HoldingHistoryView, value: Double ->
            vm.processIntent(
                HistoryIntent.UpdateEntryValue(entry.entry, value)
            )
        }
    }
    val onBrokerageChange = remember(vm) {
        { b: Brokerage -> vm.processIntent(HistoryIntent.SelectBrokerage(b)) }
    }
    val onPeriodChange = remember(vm) {
        { p: YearMonth -> vm.processIntent(HistoryIntent.SelectPeriod(p)) }
    }
    val onCategoryChange = remember(vm) {
        { c: InvestmentCategory -> vm.processIntent(HistoryIntent.SelectCategory(c)) }
    }
    val onLiquidityChange = remember(vm) {
        { l: Liquidity -> vm.processIntent(HistoryIntent.SelectLiquidity(l)) }
    }
    val onGoalChange = remember(vm) {
        { g: FinancialGoal -> vm.processIntent(HistoryIntent.SelectGoal(g)) }
    }
    val onSyncClick = remember(vm) {
        { vm.processIntent(HistoryIntent.Sync) }
    }

    HoldingHistoryScreen(
        dataRows = state.tableData,
        onValueChange = onValueChange,
        brokerageSelected = state.brokerage.selected,
        brokerageOptions = state.brokerage.options,
        onBrokerageChange = onBrokerageChange,
        periodSelected = state.period.selected!!,
        periodOptions = state.period.options,
        onPeriodChange = onPeriodChange,
        categorySelected = state.category.selected,
        categoryOptions = state.category.options,
        onCategoryChange = onCategoryChange,
        liquiditySelected = state.liquidity.selected,
        liquidityOptions = state.liquidity.options,
        onLiquidityChange = onLiquidityChange,
        goalSelected = state.goal.selected,
        goalOptions = state.goal.options,
        onGoalChange = onGoalChange,
        onSyncClick = onSyncClick,
        transactions = state.transactions
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HoldingHistoryScreen(
    dataRows: List<HoldingHistoryView>,
    onValueChange: (HoldingHistoryView, Double) -> Unit,
    brokerageSelected: Brokerage?,
    brokerageOptions: List<Brokerage>,
    onBrokerageChange: (Brokerage) -> Unit,
    periodSelected: YearMonth,
    periodOptions: List<YearMonth>,
    onPeriodChange: (YearMonth) -> Unit,
    categorySelected: InvestmentCategory?,
    categoryOptions: List<InvestmentCategory>,
    onCategoryChange: (InvestmentCategory) -> Unit,
    liquiditySelected: Liquidity?,
    liquidityOptions: List<Liquidity>,
    onLiquidityChange: (Liquidity) -> Unit,
    goalSelected: FinancialGoal?,
    goalOptions: List<FinancialGoal>,
    onGoalChange: (FinancialGoal) -> Unit,
    onSyncClick: () -> Unit,
    transactions: List<AssetTransaction>,
) {

    val scope = rememberCoroutineScope()
    val navigator: ThreePaneScaffoldNavigator<HoldingHistoryEntry> =
        rememberSupportingPaneScaffoldNavigator<HoldingHistoryEntry>()

    AppScreenScaffold(
        title = "Posicionamento no Período",
        navigator = navigator,
        actions = {
            Actions(
                showClose = navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary,
                onSyncClick = onSyncClick,
                onCloseClick = { scope.launch { navigator.navigateBack() } }
            )
        },
        mainPane = {
            Table(
                data = dataRows,
                onValueChange = onValueChange,
                onSelect = { entry: HoldingHistoryEntry ->
                    scope.launch {
                        navigator.navigateTo(
                            ThreePaneScaffoldRole.Tertiary,
                            entry
                        )
                    }
                }
            )
        },
        subMainPane = {
            SegmentedControl(
                selected = brokerageSelected?.let { SegmentedControlChoice(it, it.name) },
                options = StableList(brokerageOptions.map { SegmentedControlChoice(it, it.name) }),
                onSelect = { choice -> onBrokerageChange(choice.id) }
            )
        },
        supportingPaneWidthRate = 0.23f,
        supportingPane = {
            Supporting(
                periodSelected = periodSelected,
                periodOptions = periodOptions,
                onPeriodChange = onPeriodChange,
                categorySelected = categorySelected,
                categoryOptions = categoryOptions,
                onCategoryChange = onCategoryChange,
                liquiditySelected = liquiditySelected,
                liquidityOptions = liquidityOptions,
                onLiquidityChange = onLiquidityChange,
                goalSelected = goalSelected,
                goalOptions = goalOptions,
                onGoalChange = onGoalChange,
                dataRows = dataRows,
                transactions = transactions
            )
        },
        extraPane = {
            navigator.currentDestination?.contentKey?.let {
                TransactionPanel(selectedHolding = it.holding)
            }
        }
    )
}

@Composable
private fun Actions(
    showClose: Boolean,
    onSyncClick: () -> Unit,
    onCloseClick: () -> Unit,
) {

    if (showClose) {
        IconButton(onClick = { onCloseClick() }) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Fechar"
        )
    }
    }

    IconButton(onClick = onSyncClick) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Sincronizar"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Table(
    modifier: Modifier = Modifier,
    data: List<HoldingHistoryView>,
    onValueChange: (HoldingHistoryView, Double) -> Unit,
    onSelect: (HoldingHistoryEntry) -> Unit,
) {

    val columns = remember(onValueChange, data) {

        StableList(
            listOf<UiTableDataColumn<HoldingHistoryView>>(

                UiTableDataColumn(
                    text = "",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.category },
                    content = {
                        Row {
                            it.category.BuildIcon()
                            it.liquidity?.BuildIcon()
                        }
                    }
                ),

                UiTableDataColumn(
                    text = "Corretora",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.brokerageName }
                ),

                UiTableDataColumn(
                    text = "Display Name",
                    width = TableColumnWidth.Flex(1f),
                    comparable = { it.displayName },
                ),

                UiTableDataColumn(
                    text = "Observação",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.observations },
                ),

                UiTableDataColumn(
                    text = "Valor Anterior",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.previousValue },
                    content = { Text(it.previousValue.currencyFormat()) }
                ),

                UiTableDataColumn(
                    text = "Valor Atual",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.currentValue },
                    content = { rowData ->
                        TableInputMoney(
                            value = rowData.currentValue,
                            onValueChange = { value -> onValueChange(rowData, value ?: 0.0) },
                            enabled = rowData.isCurrentValueEnabled()
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "Balanço",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.totalBalance },
                    content = {
                        Text(
                            text = it.totalBalance.currencyFormat(),
                            color = when {
                                it.totalBalance < 0 -> getWarningColor()
                                it.totalBalance > 0 -> getInfoColor()
                                else -> Color.Gray.copy(alpha = .5f)
                            }
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "%",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.appreciation },
                    content = {
                        Text(
                            text = it.appreciation.toPercentage(),
                            color = when {
                                it.appreciation < 0 -> MaterialTheme.colorScheme.error
                                it.appreciation > 0 -> getSuccessColor()
                                else -> Color.Gray.copy(alpha = .5f)
                            }
                        )
                    }
                ),
            )
        )
    }

    UiTableV3(
        headerBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
        rows = StableList(data),
        columns = columns,
        onRowClick = { onSelect(it.entry) }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Supporting(
    periodSelected: YearMonth,
    periodOptions: List<YearMonth>,
    onPeriodChange: (YearMonth) -> Unit,
    categorySelected: InvestmentCategory?,
    categoryOptions: List<InvestmentCategory>,
    onCategoryChange: (InvestmentCategory) -> Unit,
    liquiditySelected: Liquidity?,
    liquidityOptions: List<Liquidity>,
    onLiquidityChange: (Liquidity) -> Unit,
    goalSelected: FinancialGoal?,
    goalOptions: List<FinancialGoal>,
    onGoalChange: (FinancialGoal) -> Unit,
    dataRows: List<HoldingHistoryView>,
    transactions: List<AssetTransaction>,
) {

    AppScreenPane {

        SegmentedControl(
            selected = SegmentedControlChoice(periodSelected, periodSelected.formated()),
            options = StableList(periodOptions.map { SegmentedControlChoice(it, it.formated()) }),
            onSelect = { onPeriodChange(it.id) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            fill = true
        )
    }

    AppScreenPane {

        SegmentedControl(
            selected = categorySelected?.let { SegmentedControlChoice(it, it.formated()) },
            options = StableList(categoryOptions.map { SegmentedControlChoice(it, it.formated()) }),
            onSelect = { onCategoryChange(it.id) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )

        SegmentedControl(
            selected = liquiditySelected?.let { SegmentedControlChoice(it, it.formated()) },
            options = StableList(liquidityOptions.map { SegmentedControlChoice(it, it.formated()) }),
            onSelect = { onLiquidityChange(it.id) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )

        SegmentedControl(
            selected = goalSelected?.let { SegmentedControlChoice(it, it.name) },
            options = StableList(goalOptions.map { SegmentedControlChoice(it, it.name) }),
            onSelect = { onGoalChange(it.id) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )
    }

    Summary(dataRows)

    Transactions(transactions)
}

@Composable
private fun Transactions(transactions: List<AssetTransaction>) {

    if (transactions.isEmpty()) {

        AppScreenPane(
            contentPadding = PaddingValues(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Text(modifier = Modifier.fillMaxWidth(), text = "Nenhuma transação esse mês", textAlign = TextAlign.Center)
        }

    } else {

        AppScreenPane(
            modifier = Modifier.padding(bottom = 32.dp),
        ) {

            UiTableV3(
                headerBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                rows = StableList(transactions),
                columns = StableList(
                    listOf(

                        UiTableDataColumn(
                            text = "Data",
                            width = TableColumnWidth.Flex(1f),
                            comparable = { it.date },
                            content = { Text(it.date.formated()) }
                        ),

                        UiTableDataColumn(
                            text = "Transação",
                            width = TableColumnWidth.Flex(1f),
                            comparable = { it.type },
                            content = { Text(it.type.formated()) }
                        ),

                        UiTableDataColumn(
                            text = "Valor",
                            width = TableColumnWidth.Flex(1f),
                            comparable = { it.totalValue },
                            content = { Text(it.totalValue.currencyFormat()) }
                        )
                    )
                ),
                footer = {
                    Text("")
                }
            )
        }
    }
}

@Composable
private fun Summary(rows: List<HoldingHistoryView>) { // TODO mover calculos para usecase

    AppScreenPane {

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {

            val prev = rows.sumOf { it.previousValue }
            val act = rows.sumOf { it.currentValue }
            val bal = rows.sumOf { it.totalBalance }

            val calc = Appreciation.calculate(
                previousValue = prev,
                currentValue = act,
                contributions = bal,
                withdrawals = 0.0
            )

            listOf(
                "Valor Anterior" to prev.currencyFormat(),
                "Valor Atual" to act.currencyFormat(),
                "Aportes" to "--",
                "Retiradas" to "--",
                "Balanço / Crescimento" to bal.currencyFormat(),
                "%" to "--",
                "Valorização" to "--",
                "%" to calc.percentage.toPercentage()
            ).forEach { (title, value) ->

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                value,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
