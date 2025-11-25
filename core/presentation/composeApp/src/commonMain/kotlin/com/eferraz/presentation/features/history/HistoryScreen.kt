package com.eferraz.presentation.features.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.InputTextMoney
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.design_system.components.panels.Pane
import com.eferraz.presentation.design_system.components.panels.Section
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun HistoryRoute() {

    val vm = koinViewModel<HistoryViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(24.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Posicionamento no Período",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.weight(1f)
            )
//            PeriodSelector(
//                selectedPeriod = state.selectedPeriod,
//                onPeriodSelected = { vm.selectPeriod(it) }
//            )
        }

        val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

        SupportingPaneScaffold(
            modifier = Modifier.alpha(1f).padding(top = 32.dp),
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
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    entries: List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>,
    onUpdateValue: (Long?, Long, Double) -> Unit,
) {

    DataTable(
        modifier = modifier,
        columns = listOf(
            TableColumn(title = "Corretora", extractValue = { it.formatted.brokerage }),
            TableColumn(title = "Categoria", extractValue = { it.formatted.category }),
            TableColumn(title = "SubCategoria", extractValue = { it.formatted.subCategory }),
            TableColumn(title = "Descrição", weight = 2f, extractValue = { it.formatted.description }),
            TableColumn(title = "Observações", weight = 2f, extractValue = { it.formatted.observations }),
            TableColumn(title = "Vencimento", extractValue = { it.formatted.maturity }, sortComparator = { it.sort.maturity }),
            TableColumn(title = "Emissor", extractValue = { it.formatted.issuer }),
            TableColumn(title = "Valor Anterior", extractValue = { it.formatted.previousValue }, sortComparator = { it.sort.previousValue }),
            TableColumn(title = "Valor Atual", sortComparator = { it.sort.currentValue }, cellContent = { item ->
                InputTextMoney(value = item.sort.currentValue, onValueChange = { onUpdateValue(item.currentEntryId, item.holdingId, it ?: 0.0) })
            }),
            TableColumn(title = "Valorização", extractValue = { it.formatted.appreciation }),
            TableColumn(title = "Situação", extractValue = { it.formatted.situation })
        ),
        data = entries.map { (holding, current, preview) -> HoldingHistoryView.create(holding, current, preview) },
    )
}

