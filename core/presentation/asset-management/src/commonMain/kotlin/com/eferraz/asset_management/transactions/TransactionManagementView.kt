package com.eferraz.asset_management.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.design_system.components.inputs.TableInputDate
import com.eferraz.design_system.components.inputs.TableInputMoney
import com.eferraz.design_system.components.inputs.TableInputSelect
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.transactions.TransactionType
import com.seanproctor.datatable.TableColumnWidth
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TransactionFormView(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onComplete: () -> Unit,
) {

    val vm = koinViewModel<TransactionManagementViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(holdingId) {
        vm.dispatch(TransactionManagementEvents.ScreenEntered(holdingId = holdingId))
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onComplete()
    }

    TransactionFormView(
        modifier = modifier,
        ui = state,
        onEvent = vm::dispatch
    )
}

@Composable
private fun TransactionFormView(
    modifier: Modifier,
    ui: TransactionManagementUiState,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        TransactionContent(
            ui = ui,
            onEvent = onEvent,
        )

        Spacer(Modifier.weight(1f))

        Actions(
            ui = ui,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun TransactionContent(
    ui: TransactionManagementUiState,
    onEvent: (TransactionManagementEvents) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Text(
            text = "Transações",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        TransactionTable(
            modifier = Modifier.fillMaxWidth(),
            rows = ui.transactions,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun TransactionTable(
    modifier: Modifier,
    rows: List<TransactionDraftUi>,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    val headerColor = Color(0xFFE6DBF7)
    val columns = buildColumns(rows, onEvent)

    val footer = @Composable {
        Box(
            modifier = Modifier.fillMaxWidth().height(30.dp).clickable {
                onEvent(TransactionManagementEvents.AddTransactionDraft)
            },
            contentAlignment = Alignment.Center,
        ) {
            Text("Adicionar", style = MaterialTheme.typography.bodySmall)
        }
    }

    UiTableV3(
        modifier = modifier,
        columns = StableList(columns),
        rows = StableList(rows),
        footer = footer,
        headerBackgroundColor = headerColor,
        footerBackgroundColor = headerColor,
        rowBackgroundColor = Color(0xFFF4F5F9),
    )
}

private fun buildColumns(
    rows: List<TransactionDraftUi>,
    onEvent: (TransactionManagementEvents) -> Unit,
): List<UiTableDataColumn<TransactionDraftUi>> {

    val common = mutableListOf<UiTableDataColumn<TransactionDraftUi>>(

        UiTableDataColumn(
            text = "Data",
            comparable = { it.dateDigits },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputDate(
                    value = row.dateDigits,
                    onValueChange = { onEvent(TransactionManagementEvents.DraftTransactionDateChanged(index, it)) },
                    isError = row.dateError,
                )
            },
        ),

        UiTableDataColumn(
            text = "Transação",
            comparable = { it.type.name },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputSelect(
                    value = row.type,
                    options = TransactionType.entries.toList(),
                    format = { it.asLabel() },
                    onValueChange = { onEvent(TransactionManagementEvents.DraftTransactionTypeChanged(index, it)) },
                )
            },
        ),
    )

    val category: InvestmentCategory = rows.firstOrNull()?.category ?: InvestmentCategory.FIXED_INCOME

    if (category == InvestmentCategory.VARIABLE_INCOME) {

        common += UiTableDataColumn(
            text = "Quantidade",
            alignment = Alignment.CenterEnd,
            comparable = { it.quantity },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputMoney(
                    value = row.quantity.toDoubleOrNull() ?: 0.0,
                    onValueChange = {
                        onEvent(TransactionManagementEvents.DraftTransactionQuantityChanged(index, (it ?: 0.0).toString()))
                    },
                    isError = row.quantityError,
                )
            },
        )

        common += UiTableDataColumn(
            text = "Unitário",
            alignment = Alignment.CenterEnd,
            comparable = { it.unitPrice },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputMoney(
                    value = row.unitPrice.toDoubleOrNull() ?: 0.0,
                    onValueChange = {
                        onEvent(TransactionManagementEvents.DraftTransactionUnitPriceChanged(index, (it ?: 0.0).toString()))
                    },
                    isError = row.unitPriceError,
                )
            },
        )
    }

    common += UiTableDataColumn(
        text = "Valor Total",
        alignment = Alignment.CenterEnd,
        comparable = { it.totalValue },
        content = { row ->
            val index = rows.indexOf(row)
            TableInputMoney(
                value = row.totalValue.toDoubleOrNull() ?: 0.0,
                onValueChange = {
                    onEvent(TransactionManagementEvents.DraftTransactionTotalValueChanged(index, (it ?: 0.0).toString()))
                },
                isError = row.totalValueError,
                enabled = category != InvestmentCategory.VARIABLE_INCOME,
            )
        },
    )

    common += UiTableDataColumn(
        text = "",
        alignment = Alignment.Center,
        comparable = { it.id ?: 0L },
        width = TableColumnWidth.MaxIntrinsic,
        content = { row ->
            val index = rows.indexOf(row)
            IconButton(onClick = { onEvent(TransactionManagementEvents.DraftTransactionDeleteClicked(index)) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover transação",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
    )

    return common
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onEvent: (TransactionManagementEvents) -> Unit,
    ui: TransactionManagementUiState,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.Bottom,
    ) {

        Button(
            onClick = { onEvent(TransactionManagementEvents.Save) },
            enabled = !ui.isSaving,
        ) {
            Text("Salvar")
        }
    }
}

private class TransactionFormPreviewProvider : PreviewParameterProvider<TransactionManagementUiState> {
    override val values: Sequence<TransactionManagementUiState> = sequenceOf(
        TransactionManagementUiState(
            transactions = listOf(
                TransactionDraftUi(category = InvestmentCategory.FIXED_INCOME, dateDigits = "20250110", type = TransactionType.PURCHASE, totalValue = "5000.00"),
                TransactionDraftUi(category = InvestmentCategory.FIXED_INCOME, dateDigits = "20250215", type = TransactionType.PURCHASE, totalValue = "3500.00"),
                TransactionDraftUi(category = InvestmentCategory.FIXED_INCOME, dateDigits = "20250320", type = TransactionType.SALE, totalValue = "1200.00"),
                TransactionDraftUi(category = InvestmentCategory.FIXED_INCOME, dateDigits = "20250401", type = TransactionType.PURCHASE, totalValue = "8000.00"),
                TransactionDraftUi(category = InvestmentCategory.FIXED_INCOME, dateDigits = "20250510", type = TransactionType.SALE, totalValue = "2700.00"),
            )
        ),
        TransactionManagementUiState(
            transactions = listOf(
                TransactionDraftUi(category = InvestmentCategory.VARIABLE_INCOME, dateDigits = "20250110", type = TransactionType.PURCHASE, quantity = "100.0", unitPrice = "28.50", totalValue = "2850.00"),
                TransactionDraftUi(category = InvestmentCategory.VARIABLE_INCOME, dateDigits = "20250202", type = TransactionType.PURCHASE, quantity = "50.0", unitPrice = "31.20", totalValue = "1560.00"),
                TransactionDraftUi(category = InvestmentCategory.VARIABLE_INCOME, dateDigits = "20250315", type = TransactionType.SALE, quantity = "30.0", unitPrice = "34.00", totalValue = "1020.00"),
                TransactionDraftUi(category = InvestmentCategory.VARIABLE_INCOME, dateDigits = "20250420", type = TransactionType.PURCHASE, quantity = "200.0", unitPrice = "29.75", totalValue = "5950.00"),
                TransactionDraftUi(category = InvestmentCategory.VARIABLE_INCOME, dateDigits = "20250505", type = TransactionType.SALE, quantity = "80.0", unitPrice = "33.10", totalValue = "2648.00"),
            )
        ),
        TransactionManagementUiState(
            transactions = listOf(
                TransactionDraftUi(category = InvestmentCategory.INVESTMENT_FUND, dateDigits = "20250110", type = TransactionType.PURCHASE, totalValue = "10000.00"),
                TransactionDraftUi(category = InvestmentCategory.INVESTMENT_FUND, dateDigits = "20250210", type = TransactionType.PURCHASE, totalValue = "5000.00"),
                TransactionDraftUi(category = InvestmentCategory.INVESTMENT_FUND, dateDigits = "20250312", type = TransactionType.SALE, totalValue = "3000.00"),
                TransactionDraftUi(category = InvestmentCategory.INVESTMENT_FUND, dateDigits = "20250415", type = TransactionType.PURCHASE, totalValue = "7500.00"),
                TransactionDraftUi(category = InvestmentCategory.INVESTMENT_FUND, dateDigits = "20250520", type = TransactionType.SALE, totalValue = "4200.00"),
            )
        ),
    )
}

@Preview
@Composable
private fun TransactionFormViewPreview(
    @PreviewParameter(TransactionFormPreviewProvider::class) ui: TransactionManagementUiState,
) {
    MaterialTheme {

        Scaffold(
            topBar = { TopAppBar(title = { Text("Transações") }) }
        ) {

            TransactionFormView(
                modifier = Modifier.padding(it),
                ui = ui,
                onEvent = {}
            )
        }
    }
}
