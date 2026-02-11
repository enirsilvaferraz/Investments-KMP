package com.eferraz.presentation.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.KeyboardOptionKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.design_system.theme.getSuccessColor
import com.eferraz.presentation.design_system.theme.getWarningColor
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.HistoryTableData
import com.seanproctor.datatable.TableColumnWidth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel

@Immutable
internal data class HoldingHistoryData(
    val entry: HoldingHistoryEntry,
    val category: InvestmentCategory,
    val issuerName: String,
    val displayName: String,
//    val liquidity: Liquidity,
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
//        liquidity = it.liquidity,
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
        brokerage = state.brokerage,
        brokerages = state.brokerages,
        onBrokerageChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectBrokerage(it)) },
        periodSelected = state.selectedPeriod,
        periodOptions = state.periods,
        onPeriodChange = { vm.processIntent(HistoryViewModel.HistoryIntent.SelectPeriod(it)) }
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
) {

    AppScreenScaffold(
        title = "Posicionamento no Período",
        actions = {
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
            FilterPane(periodSelected, periodOptions, onPeriodChange)
            Transactions()
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
                text = "Flags",
                width = TableColumnWidth.MaxIntrinsic,
                comparable = { it.category },
                content = { Category(it.category) }
            ),

//            UiTableDataColumn(
//                text = "Emissor",
//                width = TableColumnWidth.MaxIntrinsic,
//                comparable = { it.issuerName }
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
                content = { Text(it.totalBalance.currencyFormat()) }
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

//            UiTableDataColumn(
//                text = "Liquidez",
//                width = TableColumnWidth.MaxIntrinsic,
//                alignment = Alignment.Center,
//                comparable = { it.liquidity },
//                content = { Liquidity(it.liquidity) }
//            ),
        ),
        subFooter = listOf(
            "",
            "",
            "",
            "",
            data.sumOf { it.previousValue }.currencyFormat(),
            data.sumOf { it.currentValue }.currencyFormat(),
            data.sumOf { it.totalBalance }.currencyFormat(),
            ""
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilterPane(periodSelected: YearMonth, periodOptions: List<YearMonth>, onPeriodChange: (YearMonth) -> Unit) {


    AppScreenPane {
//        Text("Período", modifier = Modifier.padding(bottom = 8.dp))


        SegmentedControl(
            selected = periodSelected,
            options = periodOptions,
            optionDisplay = { it.formated() },
            onSelect = onPeriodChange,
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
    }


//    AppScreenPane {
//        Text("Corretora", modifier = Modifier.padding(bottom = 8.dp))
//        var brokerage by remember { mutableStateOf("Nubank") }
//        SegmentedControl(
//            selected = brokerage,
//            options = listOf("Nubank", "Bradesco", "Santander", "Itaú", "Banco do Brasil", "Inter"),
//            onSelect = { brokerage = it }
//        )
//    }


    AppScreenPane {

        var r1 by remember { mutableStateOf("") }

//        Text("Tipo de Investimento", modifier = Modifier.padding(bottom = 8.dp))
        SegmentedControl(
            selected = r1,
            options = listOf("Renda Fixa", "Renda Variável", "Fundos"),
            onSelect = { r1 = it },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        )
//    }
//
//    AppScreenPane {
        var r2 by remember { mutableStateOf("") }

//        Text("Liquidez", modifier = Modifier.padding(bottom = 8.dp))
        SegmentedControl(
            selected = r2,
            options = listOf("Liquidez Diária", "No Vencimento"),
            onSelect = { r2 = it },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        )
//    }
//
//    AppScreenPane {
        var r3 by remember { mutableStateOf("") }

//        Text("Objetivo", modifier = Modifier.padding(bottom = 8.dp))
        SegmentedControl(
            selected = r3,
            options = listOf("Apartamento", "Aposentadoria", "Automóvel"),
            onSelect = { r3 = it },
            colors = ToggleButtonDefaults.toggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        )
    }
}

@Composable
private fun Transactions() {

    data class TransactionView(val date: LocalDate, val type: TransactionType, val value: Double)

//    Text("Transações", style = MaterialTheme.typography.titleMedium)

    if (false) AppScreenPane {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            TextField(
                modifier = Modifier.weight(1f),
                value = "2026-01-01",
                label = { Text("Data") },
                onValueChange = {}
            )

            TextField(
                modifier = Modifier.weight(1f),
                value = "Compra",
                label = { Text("Transação") },
                onValueChange = {}
            )

            TextField(
                modifier = Modifier.weight(1f),
                prefix = { Text("R$ ") },
                value = "10.000,00",
                label = { Text("Valor") },
                onValueChange = {}
            )

            FilledTonalIconButton(
                onClick = { /*TODO*/ },
            ) {
                Icon(imageVector = Icons.Default.KeyboardOptionKey, contentDescription = null)
            }
        }
    }

    AppScreenPane {

        UiTableV3(
            rows = listOf(
                TransactionView(LocalDate(2026, 10, 4), TransactionType.PURCHASE, 100.0),
                TransactionView(LocalDate(2026, 10, 5), TransactionType.SALE, 200.0),
                TransactionView(LocalDate(2026, 10, 6), TransactionType.PURCHASE, 300.0),
                TransactionView(LocalDate(2026, 10, 7), TransactionType.SALE, 400.0)
            ),
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
                    comparable = { it.value },
                    content = { Text(it.value.currencyFormat()) }
                )
            ),
//            subFooter = TransactionView(LocalDate(2026, 10, 4), TransactionType.PURCHASE, 100.0),
            footer = {
                Text("")
            }
        )
    }

    AppScreenPane(
        contentPadding = PaddingValues(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(modifier = Modifier.fillMaxWidth(), text = "Nenhuma transação esse mês", textAlign = TextAlign.Center)
    }

    if (false) AppScreenPane {

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),//.height(150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Lucro Total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)

                    Text(
                        "1000,00",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),//.height(150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Valorização", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)

                    Text(
                        "1.5%",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

/**
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Liquidity(liquidity: Liquidity) {

    val icon = when (liquidity) {
        Liquidity.DAILY -> Icons.Default.EventAvailable
        else -> Icons.Default.EventBusy
    }

    val color = when (liquidity) {
        Liquidity.DAILY -> Color.Green
        else -> Color.Red
    }

    val tooltipText = liquidity.formated()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.End),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = liquidity.formated(),
            modifier = Modifier.alpha(0.5f),//.size(18.dp),
            tint = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Category(category: InvestmentCategory) {

    val icon = Icons.Default.AttachMoney

    val color = when (category) {
        InvestmentCategory.FIXED_INCOME -> getSuccessColor()
        InvestmentCategory.VARIABLE_INCOME -> MaterialTheme.colorScheme.error
        InvestmentCategory.INVESTMENT_FUND -> getWarningColor()
    }

    val tooltipText = category.formated()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.End),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltipText,
            modifier = Modifier.alpha(0.5f),//.size(18.dp),
            tint = color
        )
    }
}

//@Preview(widthDp = 2000, heightDp = 1000)
////@Preview(widthDp = 1200, heightDp = 600)
//@Composable
//public fun HoldingHistoryScreenPreview() {
//    AppTheme {
//        NavigationSuiteScaffold(
//            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
//            navigationItemVerticalArrangement = Arrangement.Center,
//            navigationItems = @Composable {
//                for (i in 0..<5) {
//                    NavigationSuiteItem(
//                        icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
//                        label = { Text("Ativos") },
//                        selected = i == 2,
//                        onClick = { }
//                    )
//                }
//            }
//        ) {
//            HoldingHistoryScreen(
//                rows = listOf(
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Nubank",
//                        displayName = "CDB 100% do CDI (venc. 2026.12.01)",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Investimento de liquidez diária",
//                        previousValue = 10000.0,
//                        currentValue = 10856.0,
//                        appreciation = 8.56,
//                        totalBalance = 856.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Banco Inter",
//                        displayName = "CDB 110% do CDI (venc. 2027.03.15)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Rendimento acima do CDI",
//                        previousValue = 15000.0,
//                        currentValue = 16245.0,
//                        appreciation = 8.30,
//                        totalBalance = 1245.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "BTG Pactual",
//                        displayName = "LCI 95% do CDI (venc. 2026.06.30)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Isento de IR",
//                        previousValue = 20000.0,
//                        currentValue = 21340.0,
//                        appreciation = 6.70,
//                        totalBalance = 1340.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "XP Investimentos",
//                        displayName = "Tesouro Selic 2029",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Título público federal",
//                        previousValue = 25000.0,
//                        currentValue = 27125.0,
//                        appreciation = 8.50,
//                        totalBalance = 2125.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Itaú",
//                        displayName = "CDB 105% do CDI (venc. 2028.01.20)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Prazo longo, maior rentabilidade",
//                        previousValue = 30000.0,
//                        currentValue = 32850.0,
//                        appreciation = 9.50,
//                        totalBalance = 2850.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "C6 Bank",
//                        displayName = "LCA 90% do CDI (venc. 2026.09.10)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Crédito do agronegócio",
//                        previousValue = 12000.0,
//                        currentValue = 12768.0,
//                        appreciation = 6.40,
//                        totalBalance = 768.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Santander",
//                        displayName = "CDB 108% do CDI (venc. 2027.11.05)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Banco tradicional",
//                        previousValue = 18000.0,
//                        currentValue = 19548.0,
//                        appreciation = 8.60,
//                        totalBalance = 1548.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Rico",
//                        displayName = "Tesouro IPCA+ 2035",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Proteção contra inflação",
//                        previousValue = 22000.0,
//                        currentValue = 24420.0,
//                        appreciation = 11.00,
//                        totalBalance = 2420.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Bradesco",
//                        displayName = "CDB 102% do CDI (venc. 2026.08.15)",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Liquidez diária disponível",
//                        previousValue = 8000.0,
//                        currentValue = 8536.0,
//                        appreciation = 6.70,
//                        totalBalance = 536.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Modal",
//                        displayName = "LCI 88% do CDI (venc. 2027.02.28)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Sem IR, vencimento médio",
//                        previousValue = 14000.0,
//                        currentValue = 14868.0,
//                        appreciation = 6.20,
//                        totalBalance = 868.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Safra",
//                        displayName = "CDB 112% do CDI (venc. 2028.05.10)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Alta rentabilidade",
//                        previousValue = 35000.0,
//                        currentValue = 38885.0,
//                        appreciation = 11.10,
//                        totalBalance = 3885.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Warren",
//                        displayName = "Tesouro Prefixado 2027",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Taxa prefixada de 11,5% a.a.",
//                        previousValue = 16000.0,
//                        currentValue = 17472.0,
//                        appreciation = 9.20,
//                        totalBalance = 1472.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "Banco BS2",
//                        displayName = "CDB 115% do CDI (venc. 2027.07.20)",
////                        liquidity = Liquidity.AT_MATURITY,
//                        observations = "Banco médio, maior rentabilidade",
//                        previousValue = 9000.0,
//                        currentValue = 9801.0,
//                        appreciation = 8.90,
//                        totalBalance = 801.0
//                    ),
//                    HoldingHistoryData(
//                        category = InvestmentCategory.FIXED_INCOME,
//                        issuerName = "PagBank",
//                        displayName = "CDB 98% do CDI (venc. 2026.04.30)",
////                        liquidity = Liquidity.DAILY,
//                        observations = "Liquidez imediata",
//                        previousValue = 11000.0,
//                        currentValue = 11726.0,
//                        appreciation = 6.60,
//                        totalBalance = 726.0
//                    )
//                ),
//                brokerage = Brokerage(0, "Nubank"),
//                brokerages = listOf(Brokerage(0, "Nubank")),
//                onBrokerageChange = {},
//                periodSelected = YearMonth(2026, 1),
//                periodOptions = listOf(YearMonth(2025, 12), YearMonth(2026, 1), YearMonth(2026, 2)),
//                onPeriodChange = {}
//            )
//        }
//    }
//}