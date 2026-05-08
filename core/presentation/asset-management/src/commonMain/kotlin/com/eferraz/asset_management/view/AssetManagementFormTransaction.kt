package com.eferraz.asset_management.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.asset_management.vm.TransactionDraftUi
import com.eferraz.asset_management.vm.TransactionFormEvent
import com.eferraz.asset_management.vm.TransactionFormUiState
import com.eferraz.design_system.components.inputs.TableInputDate
import com.eferraz.design_system.components.inputs.TableInputMoney
import com.eferraz.design_system.components.inputs.TableInputSelect
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.transactions.TransactionType
import com.seanproctor.datatable.TableColumnWidth

@Composable
internal fun TransactionFormContent(
    modifier: Modifier = Modifier,
    ui: TransactionFormUiState,
    onEvent: (TransactionFormEvent) -> Unit,
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
    onEvent: (TransactionFormEvent) -> Unit,
) {

    val headerColor = Color(0xFFE6DBF7)
    val columns = buildColumns(rows, onEvent)

    val footer = @Composable {
        Box(
            modifier = Modifier.fillMaxWidth().height(30.dp).clickable {
                onEvent(TransactionFormEvent.AddTransactionDraft)
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
    onEvent: (TransactionFormEvent) -> Unit,
): List<UiTableDataColumn<TransactionDraftUi>> {

    val common = mutableListOf<UiTableDataColumn<TransactionDraftUi>>(

        UiTableDataColumn(
            text = "Data",
            comparable = { it.dateDigits },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputDate(
                    value = row.dateDigits,
                    onValueChange = { onEvent(TransactionFormEvent.DraftTransactionDateChanged(index, it)) },
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
                    onValueChange = { onEvent(TransactionFormEvent.DraftTransactionTypeChanged(index, it)) },
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
                        onEvent(TransactionFormEvent.DraftTransactionQuantityChanged(index, (it ?: 0.0).toString()))
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
                        onEvent(TransactionFormEvent.DraftTransactionUnitPriceChanged(index, (it ?: 0.0).toString()))
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
                    onEvent(TransactionFormEvent.DraftTransactionTotalValueChanged(index, (it ?: 0.0).toString()))
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
            IconButton(onClick = { onEvent(TransactionFormEvent.DraftTransactionDeleteClicked(index)) }) {
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
