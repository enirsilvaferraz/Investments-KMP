package com.eferraz.presentation.features.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.InputTextMoney
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.design_system.components.panels.Pane
import com.eferraz.presentation.design_system.components.panels.Section
import com.eferraz.presentation.design_system.theme.getAppreciationTextColor
import com.eferraz.presentation.design_system.theme.getSituationTextColor
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Surface (

    ){

        Scaffold(
            modifier = Modifier.padding(horizontal = 32.dp),
            topBar = {
                TopAppBar(
                    title = { Text("Posicionamento no Período") },
                    actions = {
                        Selector(selected = state.selectedPeriod, periods = state.periods, onSelect = { vm.selectPeriod(it) })
                    }
                )
            }) {

            val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

            SupportingPaneScaffold(
                modifier = Modifier.padding(it),
                directive = navigator.scaffoldDirective.copy(horizontalPartitionSpacerSize = 24.dp),
                value = navigator.scaffoldValue,
                mainPane = {
                    Pane {
                        Section(Modifier.fillMaxSize()) {
                            HistoryScreen(entries = state.entries, onUpdateValue = vm::updateEntryValue)
                        }
                    }
                },
                supportingPane = {
                    // Empty
                },
                extraPane = {
                    Pane {
                        Section(Modifier.fillMaxSize()) {}
                    }
                },
            )
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PeriodSelector(
    selectedPeriod: YearMonth,
    onPeriodSelected: (YearMonth) -> Unit,
) {

    val currentYearMonth = YearMonth(2025, 12)
    val periodOptions = (0..23).map { _ -> currentYearMonth.minusMonth() }

    var expanded by remember { mutableStateOf(false) }
    val selectedPeriodText = selectedPeriod.formated()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = selectedPeriodText,
            onValueChange = { },
            readOnly = true,
            label = { Text("Período") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            periodOptions.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.formated()) },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun Selector(
    modifier: Modifier = Modifier,
    selected: YearMonth,
    periods: List<YearMonth>,
    onSelect: (YearMonth) -> Unit,
) {

//    Section {

    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier,
//            horizontalArrangement = spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {

        items(periods) { current ->

            if (selected == current || expanded) FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = {
                    expanded = !expanded
                    if (selected != current) {
                        onSelect(current)
                    }
                },
                label = { Text(current.formated()) },
                selected = current == selected,
                colors = FilterChipDefaults.filterChipColors(containerColor = if (current == selected) colors.primaryContainer else colors.surface)
            )
        }
    }
//    }
}

@Composable
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    entries: List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>,
    onUpdateValue: (Long?, Long, Double) -> Unit,
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
                alignment = Alignment.End
            ),

            TableColumn(
                title = "Valor Atual",
                data = { viewData.currentValue },
                alignment = Alignment.CenterHorizontally,
                cellContent = { item ->
                    InputTextMoney(
                        value = item.viewData.currentValue,
                        enabled = item.viewData.editable,
                        onValueChange = { onUpdateValue(item.currentEntryId, item.holdingId, it ?: 0.0) }
                    )
                }
            ),

            TableColumn(
                title = "Valorização",
                data = { viewData.appreciation },
                alignment = Alignment.CenterHorizontally,
//                cellContent = { item ->
//                    Text(
//                        text = item.viewData.appreciation,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = getAppreciationTextColor(item.viewData.appreciationValue)
//                    )
//                }
            ),

            TableColumn(
                title = "Situação",
                data = { viewData.situation },
                alignment = Alignment.CenterHorizontally,
//                cellContent = { item ->
//                    Text(
//                        text = item.viewData.situation,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = getSituationTextColor(item.viewData.situation)
//                    )
//                }
            )
        ),
        data = entries.map { (holding, current, preview) -> HoldingHistoryRow.create(holding, current, preview) },
    )
}

