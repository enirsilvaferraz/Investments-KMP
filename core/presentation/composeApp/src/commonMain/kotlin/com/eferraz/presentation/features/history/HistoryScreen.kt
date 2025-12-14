package com.eferraz.presentation.features.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.InputTextMoney
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.usecases.HoldingHistoryResult
import kotlinx.datetime.YearMonth
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    AppScaffold(
        title = "Posicionamento no Período",
        actions = {
            HistoryActions(
                selected = state.selectedPeriod,
                periods = state.periods,
                onSelect = { vm.processIntent(HistoryIntent.SelectPeriod(it)) }
            )
        },
        mainPane = {
            HistoryScreen(
                entries = state.entries,
                onUpdateValue = { entry, value ->
                    vm.processIntent(HistoryIntent.UpdateEntryValue(entry, value))
                }
            )
        }
    )
}

@Composable
private fun HistoryActions(
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
private fun HistoryScreen(
    modifier: Modifier = Modifier,
    entries: List<HoldingHistoryResult>,
    onUpdateValue: (HoldingHistoryEntry, Double) -> Unit,
) {

    DataTable(
        modifier = modifier,
        columns = listOf(

            TableColumn(
                title = "Corretora",
                data = { viewData.brokerage }
            ),

            TableColumn(
                title = "Categoria",
                data = { viewData.category }
            ),

            TableColumn(
                title = "SubCategoria",
                data = { viewData.subCategory }
            ),

            TableColumn(
                title = "Descrição",
                data = { viewData.description },
                weight = 2f
            ),

            TableColumn(
                title = "Vencimento",
                data = { viewData.maturity },
                formated = { formatted.maturity },
                alignment = Alignment.CenterHorizontally
            ),

            TableColumn(
                title = "Emissor",
                data = { viewData.issuer }
            ),

            TableColumn(
                title = "Observações",
                data = { viewData.observations },
                weight = 2f
            ),

            TableColumn(
                title = "Valor Anterior",
                data = { viewData.previousValue },
                formated = { formatted.previousValue },
                alignment = Alignment.End,
                footerOperation = { it: List<HoldingHistoryRow> ->
                    it.sumOf { it.viewData.previousValue }.let {
                        if (it == 0.0) null else it.currencyFormat()
                    }
                }
            ),

            TableColumn(
                title = "Valor Atual",
                data = { viewData.currentValue },
                alignment = Alignment.CenterHorizontally,
                cellContent = { item ->
                    InputTextMoney(
                        value = item.viewData.currentValue,
                        enabled = item.viewData.editable,
                        onValueChange = { onUpdateValue(item.currentHistory, it ?: 0.0) }
                    )
                },
                footerOperation = { it: List<HoldingHistoryRow> ->
                    it.sumOf { it.viewData.currentValue }.let {
                        if (it == 0.0) null else it.currencyFormat()
                    }
                }
            ),

//            TableColumn(
//                title = "Valorização",
//                data = { viewData.appreciation },
//                alignment = Alignment.CenterHorizontally,
//            ),
//
//            TableColumn(
//                title = "Situação",
//                data = { viewData.situation },
//                alignment = Alignment.CenterHorizontally,
//            )
        ),
        data = entries.map { result -> HoldingHistoryRow.create(result.holding, result.currentEntry, result.previousEntry) },
    )
}

