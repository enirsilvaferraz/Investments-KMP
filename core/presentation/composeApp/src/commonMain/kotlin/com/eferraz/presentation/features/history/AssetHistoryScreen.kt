package com.eferraz.presentation.features.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.design_system.components.segmented_control.SegmentedControl
import com.eferraz.design_system.components.segmented_control.SegmentedControlChoice
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.design_system.scaffolds.AppScreenPane
import com.eferraz.design_system.scaffolds.AppScreenScaffold
import com.eferraz.design_system.theme.getInfoColor
import com.eferraz.design_system.theme.getSuccessColor
import com.eferraz.design_system.theme.getWarningColor
import com.eferraz.design_system.theme.historyMutedTextColor
import com.eferraz.design_system_v2.dateselector.MonthYearSelector
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.naming.BuildCell
import com.eferraz.naming.BuildIcon
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.features.summary.SummaryGridWidget
import com.eferraz.presentation.features.walletfilters.WalletFiltersPanel
import com.eferraz.presentation.features.walletfilters.WalletFiltersPanelOptions
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.presentation.features.walletfilters.toggleBrokerage
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.B3IdentifierStatus
import com.eferraz.usecases.entities.HoldingHistoryRow
import com.eferraz.usecases.entities.MonthSummary
import com.seanproctor.datatable.TableColumnWidth
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun HoldingHistoryRoute(
    onEditHolding: (Long) -> Unit,
) {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.period.selected == null) return

    val onValueChange = remember(vm) {
        { entry: HoldingHistoryRow, value: Double ->
            vm.processIntent(
                HistoryIntent.UpdateEntryValue(entry.entry, value)
            )
        }
    }
    val onPeriodChange = remember(vm) {
        { p: YearMonth -> vm.processIntent(HistoryIntent.SelectPeriod(p)) }
    }
    val onWalletFiltersChange = remember(vm) {
        { filters: WalletFiltersUiState -> vm.processIntent(HistoryIntent.WalletFiltersChanged(filters)) }
    }
    val onSyncClick = remember(vm) {
        { vm.processIntent(HistoryIntent.Sync) }
    }
    val onExportFixedIncomeClick = remember(vm) {
        { vm.processIntent(HistoryIntent.ExportFixedIncomeCsv) }
    }
    val onImportClick = remember(vm) {
        { vm.processIntent(HistoryIntent.ImportB3File) }
    }
    val onBalancingClick = remember(vm) {
        { vm.processIntent(HistoryIntent.CalculatePortfolioBalancing) }
    }

    HoldingHistoryScreen(
        dataRows = state.tableData,
        onValueChange = onValueChange,
        periodSelected = state.period.selected!!,
        periodOptions = state.period.options,
        onPeriodChange = onPeriodChange,
        walletFilterOptions = state.walletFilterOptions,
        walletFilters = state.walletFilters,
        onWalletFiltersChange = onWalletFiltersChange,
        onSyncClick = onSyncClick,
        onExportFixedIncomeClick = onExportFixedIncomeClick,
        isImporting = state.isImporting,
        onImportClick = onImportClick,
        onBalancingClick = onBalancingClick,
        onEditHolding = onEditHolding,
        monthSummary = state.monthSummary,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HoldingHistoryScreen(
    dataRows: List<HoldingHistoryRow>,
    onValueChange: (HoldingHistoryRow, Double) -> Unit,
    periodSelected: YearMonth,
    periodOptions: List<YearMonth>,
    onPeriodChange: (YearMonth) -> Unit,
    walletFilterOptions: WalletFiltersPanelOptions,
    walletFilters: WalletFiltersUiState,
    onWalletFiltersChange: (WalletFiltersUiState) -> Unit,
    onSyncClick: () -> Unit,
    onExportFixedIncomeClick: () -> Unit,
    isImporting: Boolean,
    onImportClick: () -> Unit,
    onBalancingClick: () -> Unit,
    onEditHolding: (Long) -> Unit,
    monthSummary: MonthSummary,
) {

    val scope = rememberCoroutineScope()
    val navigator: ThreePaneScaffoldNavigator<HoldingHistoryEntry> =
        rememberSupportingPaneScaffoldNavigator<HoldingHistoryEntry>()

    AppScreenScaffold(
        title = "Posicionamento no Período",
        navigator = navigator,
        actions = {
            Actions(
                periodSelected = periodSelected,
                periodOptions = periodOptions,
                onPeriodChange = onPeriodChange,
                showClose = navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary,
                onSyncClick = onSyncClick,
                isImporting = isImporting,
                onImportClick = onImportClick,
                onBalancingClick = onBalancingClick,
                onExportFixedIncomeClick = onExportFixedIncomeClick,
                onCloseClick = { scope.launch { navigator.navigateBack() } }
            )
        },
        mainPane = {
            Table(
                data = dataRows,
                onValueChange = onValueChange,
                onSelect = { entry: HoldingHistoryEntry -> onEditHolding(entry.holding.id) }
            )
        },
        subMainPane = {
            SegmentedControl(
                selected = walletFilters.selectedBrokerage?.let { SegmentedControlChoice(it, it.name) },
                options = StableList(walletFilterOptions.commons.brokerageOptions.map { SegmentedControlChoice(it.id, it.id.name) }),
                onSelect = { choice ->
                    onWalletFiltersChange(walletFilters.toggleBrokerage(choice.id))
                },
            )
        },
        supportingPaneWidthRate = 0.255f,
        supportingPane = {
            Supporting(
                walletFilterOptions = walletFilterOptions,
                walletFilters = walletFilters,
                onWalletFiltersChange = onWalletFiltersChange,
                monthSummary = monthSummary,
            )
        }
    )
}

@Composable
private fun Actions(
    periodSelected: YearMonth,
    periodOptions: List<YearMonth>,
    onPeriodChange: (YearMonth) -> Unit,
    showClose: Boolean,
    onSyncClick: () -> Unit,
    isImporting: Boolean,
    onImportClick: () -> Unit,
    onBalancingClick: () -> Unit,
    onExportFixedIncomeClick: () -> Unit,
    onCloseClick: () -> Unit,
) {

    MonthYearSelector(
        selected = periodSelected,
        options = periodOptions,
        onItemSelect = onPeriodChange,
//        modifier = Modifier.fillMaxWidth(),
    )

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

    if (isImporting) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        IconButton(onClick = onImportClick) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = "Importar posições B3"
            )
        }
    }

    IconButton(onClick = onExportFixedIncomeClick) {
        Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = "Exportar renda fixa em CSV"
        )
    }

    IconButton(onClick = onBalancingClick) {
        Icon(
            imageVector = Icons.Outlined.AccountBalance,
            contentDescription = "Balanceamento de carteira",
        )
    }
}

@Composable
private fun historyRowTextColor(row: HoldingHistoryRow): Color =
    if (row.isLiquidated) historyMutedTextColor() else LocalContentColor.current

@Composable
private fun appreciationTextColor(amount: Double): Color =
    when {
        amount < 0 -> MaterialTheme.colorScheme.error
        amount > 0 -> getSuccessColor()
        else -> historyMutedTextColor()
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Table(
    modifier: Modifier = Modifier,
    data: List<HoldingHistoryRow>,
    onValueChange: (HoldingHistoryRow, Double) -> Unit,
    onSelect: (HoldingHistoryEntry) -> Unit
) {

    val columns = remember(onValueChange, data) {

        StableList(
            listOf<UiTableDataColumn<HoldingHistoryRow>>(

                UiTableDataColumn(
                    text = "",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.assetClass },
                    content = {
                        Row {
                            it.assetClass.BuildIcon()
                            it.liquidity.BuildIcon()
                        }
                    }
                ),

                UiTableDataColumn(
                    text = "Corretora",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.brokerageName },
                    content = { row -> Text(row.brokerageName, color = historyRowTextColor(row)) }
                ),

                UiTableDataColumn(
                    text = "Display Name",
                    width = TableColumnWidth.Flex(1f),
                    comparable = { it.displayName },
                    content = { row -> Text(row.displayName, color = historyRowTextColor(row)) }
                ),

                UiTableDataColumn(
                    text = "Vencimento",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.expirationDate.formated() },
                    content = { row ->
                        val label = row.expirationDate?.formated().orEmpty()
                        Text(label, color = historyRowTextColor(row))
                    }
                ),

                UiTableDataColumn(
                    text = "Observação",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.observation },
                    content = { row -> Text(row.observation, color = historyRowTextColor(row)) }
                ),

                UiTableDataColumn(
                    text = "Valor Anterior",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.previousValue },
                    content = { row -> Text(row.previousValue.currencyFormat(), color = historyRowTextColor(row)) }
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
                            enabled = rowData.isCurrentValueEnabled(),
                            textColor = if (rowData.isLiquidated) historyMutedTextColor() else null,
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "Transações",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.Center,
                    comparable = { it.periodTransactionValue },
                    content = {
                        Text(
                            text = it.periodTransactionValue.currencyFormat(),
                            color = when {
                                it.periodTransactionValue < 0 -> getWarningColor()
                                it.periodTransactionValue > 0 -> getInfoColor()
                                else -> historyMutedTextColor()
                            }
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "Valorização",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.CenterEnd,
                    comparable = { it.appreciationValue },
                    content = { row ->
                        Text(
                            text = row.appreciationValue.currencyFormat(),
                            color = appreciationTextColor(row.appreciationValue),
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "%",
                    width = TableColumnWidth.MaxIntrinsic,
                    alignment = Alignment.Center,
                    comparable = { it.appreciationPercentage },
                    content = { row ->
                        Text(
                            text = row.appreciationPercentage.toPercentage(),
                            color = appreciationTextColor(row.appreciationPercentage),
                        )
                    }
                ),

                UiTableDataColumn(
                    text = "",
                    width = TableColumnWidth.MaxIntrinsic,
                    comparable = { it.b3IdentifierStatus.toString() },
                    content = { row ->
                        when (val status = row.b3IdentifierStatus) {
                            is B3IdentifierStatus.Informed, B3IdentifierStatus.NotInformed -> status.BuildCell()
                            B3IdentifierStatus.NotApplicable -> Unit
                        }
                    },
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

@Composable
private fun Supporting(
    walletFilterOptions: WalletFiltersPanelOptions,
    walletFilters: WalletFiltersUiState,
    onWalletFiltersChange: (WalletFiltersUiState) -> Unit,
    monthSummary: MonthSummary,
) {

    WalletFiltersPanel(
        options = walletFilterOptions,
        state = walletFilters,
        onStateChange = onWalletFiltersChange,
    )

    SummaryGridWidget(properties = monthSummary)
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
