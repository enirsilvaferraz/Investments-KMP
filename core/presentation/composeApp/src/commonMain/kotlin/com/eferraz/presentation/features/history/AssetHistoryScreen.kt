package com.eferraz.presentation.features.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.design_system.components.segmented_control.SegmentedControl
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.scaffolds.AppScreenPane
import com.eferraz.design_system.scaffolds.AppScreenScaffold
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.presentation.commons.table_icons.BuildIcon
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.design_system.theme.getInfoColor
import com.eferraz.presentation.design_system.theme.getSuccessColor
import com.eferraz.presentation.design_system.theme.getWarningColor
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.HistoryTableData
import com.seanproctor.datatable.TableColumnWidth
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel

@Immutable
internal data class HoldingHistoryData(
    val entry: HoldingHistoryEntry,
    val category: InvestmentCategory,
    val issuerName: String,
    val displayName: String,
    val liquidity: Liquidity?,
    val observations: String,
    val previousValue: Double,
    val currentValue: Double,
    val appreciation: Double,
    val totalBalance: Double,
) {

    constructor(it: HistoryTableData) : this(
        entry = it.currentEntry,
        category = it.category,
        issuerName = it.issuerName,
        displayName = it.displayName,
        liquidity = if (it is FixedIncomeHistoryTableData) it.liquidity else null,
        observations = it.observations,
        previousValue = it.previousValue,
        currentValue = it.currentValue,
        appreciation = it.appreciation,
        totalBalance = it.totalBalance
    )
}

@Composable
public fun HoldingHistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    HoldingHistoryScreen(
        rows = state.tableData.map { HoldingHistoryData(it) },
        onValueChange = { entry, value -> vm.processIntent(HistoryViewModel.HistoryIntent.UpdateEntryValue(entry.entry, value)) },
        brokerage = state.brokerage.selected,
        brokerages = state.brokerage.options,
        onBrokerageChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectBrokerage(it)) },
        periodSelected = state.period.selected!!,
        periodOptions = state.period.options,
        onPeriodChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectPeriod(it)) },
        categorySelected = state.category.selected,
        categoryOptions = state.category.options,
        onCategoryChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectCategory(it)) },
        liquiditySelected = state.liquidity.selected,
        liquidityOptions = state.liquidity.options,
        onLiquidityChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectLiquidity(it)) },
        goalSelected = state.goal.selected,
        goalOptions = state.goal.options,
        onGoalChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectGoal(it)) },
        onSyncClick = { vm.processIntent(HistoryViewModel.HistoryIntent.Sync) },
        transactions = state.transactions
    )
}


@Composable
internal fun HoldingHistoryScreen(
    rows: List<HoldingHistoryData> = emptyList(),
    onValueChange: (HoldingHistoryData, Double) -> Unit,
    brokerage: Brokerage?,
    brokerages: List<Brokerage>,
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

    AppScreenScaffold(
        title = "Posicionamento no Período",
        actions = {

            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.Sync, contentDescription = "Sincronizar"
                )
            }

//            IconButton(onClick = {}) {
//                Icon(
//                    imageVector = Icons.Default.FilterList, contentDescription = "Filtros"
//                )
//            }
        },
        mainPane = {
            Table(
                data = rows,
                onValueChange = onValueChange
            )
        },
        subMainPane = {
            SegmentedControl(
                selected = brokerage,
                options = brokerages,
                optionDisplay = { it.name },
                onSelect = onBrokerageChange
            )
        },
        supportingPaneWidthRate = 0.23f,
        supportingPane = {

            FilterPane(
                periodSelected,
                periodOptions,
                onPeriodChange,
                categorySelected,
                categoryOptions,
                onCategoryChange,
                liquiditySelected,
                liquidityOptions,
                onLiquidityChange,
                goalSelected,
                goalOptions,
                onGoalChange
            )

            Transactions(transactions, rows)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Table(
    modifier: Modifier = Modifier,
    data: List<HoldingHistoryData>,
    onValueChange: (HoldingHistoryData, Double) -> Unit,
) {

    UiTableV3(
        modifier = modifier,
        rows = data,
        columns = listOf(

            UiTableDataColumn(
                text = "",
                width = TableColumnWidth.MaxIntrinsic,
                comparable = { it.category },
                content = { it.category.BuildIcon() }
            ),

//            UiTableDataColumn(
//                text = "",
//                width = TableColumnWidth.MaxIntrinsic,
//                comparable = { it.liquidity ?: "" },
//                content = { it.liquidity?.BuildIcon() }
//            ),

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
                content = {
                    TableInputMoney(
                        value = it.currentValue,
                        onValueChange = { value -> onValueChange(it, value ?: 0.0) },
                        enabled = true
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
                            else -> Color.Gray
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
                            else -> Color.Gray
                        }
                    )
                }
            ),
        ),
//        subFooter = listOf(
//            "",
//            "",
//            "",
//            data.sumOf { it.previousValue }.currencyFormat(),
//            data.sumOf { it.currentValue }.currencyFormat(),
//            data.sumOf { it.totalBalance }.currencyFormat(),
//            ""
//        ),
//            footer = {
//                Text("")
//            }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilterPane(
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
) {

    AppScreenPane {
        SegmentedControl(
            selected = periodSelected,
            options = periodOptions,
            optionDisplay = { it.formated() },
            onSelect = onPeriodChange,
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            fill = true
        )
    }

    AppScreenPane {

        SegmentedControl(
            selected = categorySelected,
            options = categoryOptions,
            optionDisplay = { it.formated() },
            onSelect = onCategoryChange,
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )

        SegmentedControl(
            selected = liquiditySelected,
            options = liquidityOptions,
            optionDisplay = { it.formated() },
            onSelect = onLiquidityChange,
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )

        SegmentedControl(
            selected = goalSelected,
            options = goalOptions,
            optionDisplay = { it.name },
            onSelect = onGoalChange,
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            fill = true
        )
    }
}

@Composable
private fun Transactions(transactions: List<AssetTransaction>, rows: List<HoldingHistoryData>) {

    AppScreenPane {

        UiTableV3(
            rows = transactions,
            columns = listOf(

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
            ),
            footer = {
                Text("")
            }
        )
    }

    if (false) AppScreenPane(
        contentPadding = PaddingValues(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(modifier = Modifier.fillMaxWidth(), text = "Nenhuma transação esse mês", textAlign = TextAlign.Center)
    }

    AppScreenPane(
        modifier = Modifier.padding(bottom = 32.dp),
    ) {

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {

            val prev = rows.sumOf { it.previousValue }
            val act = rows.sumOf { it.currentValue }

            listOf(
                "Valor Anterior" to prev.currencyFormat(),
                "Valor Atual" to act.currencyFormat(),
                "Balanço" to rows.sumOf { it.totalBalance }.currencyFormat(),
                "Valorização" to (prev / act).toPercentage()
            ).forEach { (title, value) ->

                Column(
                    modifier = Modifier.weight(1f),//.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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