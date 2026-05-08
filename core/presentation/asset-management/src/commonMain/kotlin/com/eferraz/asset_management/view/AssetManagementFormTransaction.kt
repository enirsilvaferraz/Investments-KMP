package com.eferraz.asset_management.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.vm.TransactionDraftUi
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.components.inputs.TableInputDate
import com.eferraz.design_system.components.inputs.TableInputMoney
import com.eferraz.design_system.components.inputs.TableInputSelect
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.transactions.TransactionType

@Composable
internal fun TransactionFormContent(
    modifier: Modifier = Modifier,
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
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
            rows = ui.transactionDrafts,
            category = ui.category,
            focusedInvalidRowIndex = ui.focusedInvalidRowIndex,
            addEnabled = !ui.isSaving,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun TransactionTable(
    modifier: Modifier,
    rows: List<TransactionDraftUi>,
    category: InvestmentCategory,
    focusedInvalidRowIndex: Int?,
    addEnabled: Boolean,
    onEvent: (VMEvents) -> Unit,
) {
    val headerColor = Color(0xFFE6DBF7)
    val columns = buildColumns(rows, category, focusedInvalidRowIndex, onEvent)

    UiTableV3(
        modifier = modifier,
        columns = StableList(columns),
        rows = StableList(rows),
        footer = {
            Box(
                modifier = Modifier.fillMaxWidth().height(30.dp).clickable(enabled = addEnabled) {
                    onEvent(VMEvents.AddTransactionDraft)
                },
                contentAlignment = Alignment.Center,
            ) {
                Text("Adicionar", style = MaterialTheme.typography.bodySmall)
            }
        },
        headerBackgroundColor = headerColor,
        footerBackgroundColor = headerColor,
        rowBackgroundColor = Color(0xFFF4F5F9),
    )
}

private fun buildColumns(
    rows: List<TransactionDraftUi>,
    category: InvestmentCategory,
    focusedInvalidRowIndex: Int?,
    onEvent: (VMEvents) -> Unit,
): List<UiTableDataColumn<TransactionDraftUi>> {

    val common = mutableListOf<UiTableDataColumn<TransactionDraftUi>>(

        UiTableDataColumn(
            text = "Data",
            comparable = { it.dateDigits },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputDate(
                    value = row.dateDigits,
                    onValueChange = { onEvent(VMEvents.DraftTransactionDateChanged(index, it)) },
                    isError = row.inlineError?.contains("Data") == true || focusedInvalidRowIndex == index,
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
                    onValueChange = { onEvent(VMEvents.DraftTransactionTypeChanged(index, it)) },
                )
            },
        ),
    )

    if (category == InvestmentCategory.VARIABLE_INCOME) {

        common += UiTableDataColumn(
            text = "Quantidade",
            alignment = Alignment.CenterEnd,
            comparable = { it.quantity },
            content = { row ->
                val index = rows.indexOf(row)
                TableInputMoney(
                    value = row.quantity.toDoubleOrNull() ?: 0.0,
                    onValueChange = { onEvent(VMEvents.DraftTransactionQuantityChanged(index, (it ?: 0.0).toString())) },
                    isError = row.inlineError?.contains("Quantidade") == true || focusedInvalidRowIndex == index,
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
                    onValueChange = { onEvent(VMEvents.DraftTransactionUnitPriceChanged(index, (it ?: 0.0).toString())) },
                    isError = row.inlineError?.contains("Preço") == true || focusedInvalidRowIndex == index,
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
                onValueChange = { onEvent(VMEvents.DraftTransactionTotalValueChanged(index, (it ?: 0.0).toString())) },
                isError = row.inlineError?.contains("Valor total") == true || focusedInvalidRowIndex == index,
                enabled = category != InvestmentCategory.VARIABLE_INCOME,
            )
        },
    )

    return common
}

private fun TransactionType.asLabel(): String =
    when (this) {
        TransactionType.PURCHASE -> "Compra"
        TransactionType.SALE -> "Venda"
    }
