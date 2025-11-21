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
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.design_system.components.panels.Pane
import com.eferraz.presentation.design_system.components.panels.Section
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.compose.viewmodel.koinViewModel
import com.eferraz.presentation.helpers.Formatters.formated as formatDate

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
                        HistoryScreen(entries = state.entries)
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
    onUpdateValue: (Long?, Long, Double, Double) -> Unit = { _, _, _, _ -> },
) {

    DataTable(
        modifier = modifier,
        columns = listOf(
            TableColumn(title = "Corretora", extractValue = { it.brokerage }),
            TableColumn(title = "Categoria", extractValue = { it.category }),
            TableColumn(title = "SubCategoria", extractValue = { it.subCategory }),
            TableColumn(title = "Descrição", weight = 2f, extractValue = { it.description }),
            TableColumn(title = "Observações", weight = 2f, extractValue = { it.observations }),
            TableColumn(title = "Vencimento", extractValue = { it.maturity.formatDate() }, sortComparator = { it.maturity }),
            TableColumn(title = "Emissor", extractValue = { it.issuer }),
            TableColumn(title = "Liquidez", extractValue = { it.liquidity }),
            TableColumn(title = "Qtde. Ant.", extractValue = { it.previousQuantity?.toString() ?: "—" }),
            TableColumn(title = "Qtde. Atual", extractValue = { it.currentQuantity?.toString() ?: "" },
                cellContent = { item ->
                    EditableValueCell(
                        value = item.currentQuantity,
                        onValueChange = { onUpdateValue(item.currentEntryId, item.holdingId, item.currentValue ?: 0.0,  it.toDoubleOrNull() ?: 0.0) }
                    )
                }
            ),
            TableColumn(title = "Valor Mercado Ant.", extractValue = { it.previousValue?.currencyFormat() ?: "—" }, sortComparator = { it.previousValue }),
            TableColumn(title = "Valor Mercado Atual", extractValue = { it.currentValue?.currencyFormat() ?: "" }, sortComparator = {it.currentValue},
                cellContent = { item ->
                    EditableValueCell(
                        value = item.currentValue,
                        onValueChange = { onUpdateValue(item.currentEntryId, item.holdingId, it.toDoubleOrNull() ?: 0.0, item.currentQuantity ?: 0.0) }
                    )
                }
            ),
            TableColumn(title = "Valorização", extractValue = { it.appreciation }),
            TableColumn(title = "Situação", extractValue = { it.situation })
        ),
        data = entries.map {(holding, current, preview)-> HoldingHistoryView.create(holding, current, preview) },
    )
}

@Composable
private fun EditableValueCell(
    value: Double?,
    onValueChange: (String) -> Unit,
) {
    var textValue by remember(value) { mutableStateOf(value?.toString() ?: "") }

    TextField(
        value = textValue,
        onValueChange = {
            textValue = it
            onValueChange(it)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

