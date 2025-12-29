package com.eferraz.presentation.features.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputMoneyColumn
import com.eferraz.presentation.design_system.components.table.textColumn
import com.eferraz.presentation.features.transactions.TransactionPanel
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
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
            HistoryScreen(
                entries = state.entries,
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
private fun HistoryScreen(
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

//            textColumn(
//                title = "Categoria",
//                getValue = { it.viewData.category },
//                format = { it.viewData.category }
//            ),

//            textColumn(
//                title = "SubCategoria",
//                getValue = { it.viewData.subCategory },
//                format = { it.viewData.subCategory }
//            ),

            textColumn(
                title = "Descrição",
                getValue = { it.viewData.description },
                format = { it.viewData.description },
                weight = 2f
            ),

            textColumn(
                title = "Vencimento",
                getValue = { it.viewData.maturity },
                format = { it.formatted.maturity },
                alignment = Alignment.CenterHorizontally
            ),

            textColumn(
                title = "Emissor",
                getValue = { it.viewData.issuer },
                format = { it.viewData.issuer }
            ),

            textColumn(
                title = "Observações",
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

//            textColumn(
//                title = "Valorização",
//                getValue = { it.viewData.appreciation },
//                format = { it.viewData.appreciation },
//                alignment = Alignment.CenterHorizontally
//            ),
//
//            textColumn(
//                title = "Situação",
//                getValue = { it.viewData.situation },
//                format = { it.viewData.situation },
//                alignment = Alignment.CenterHorizontally
//            )
        ),
        data = entries.map { result -> HoldingHistoryRow.create(result.holding, result.currentEntry, result.previousEntry) },
        onRowClick = onRowClick
    )
}

