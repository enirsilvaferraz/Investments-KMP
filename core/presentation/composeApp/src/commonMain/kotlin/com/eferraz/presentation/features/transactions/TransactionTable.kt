package com.eferraz.presentation.features.transactions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.seanproctor.datatable.TableColumnWidth

@Composable
internal fun TransactionTable(
    modifier: Modifier = Modifier,
    transactions: List<TransactionRow>,
    asset: Asset?,
) {
    if (transactions.isEmpty()) {
        Text(
            text = "Não há transações cadastradas",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
//                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val isVariableIncome = asset is VariableIncomeAsset
    val columns = if (isVariableIncome) {
        buildVariableIncomeColumns()
    } else {
        buildFixedIncomeOrFundsColumns()
    }

    UiTableV3(
        modifier = modifier.fillMaxWidth(),
        columns = columns,
        rows = transactions,
    )
}

@Composable
private fun buildVariableIncomeColumns(): List<UiTableDataColumn<TransactionRow>> {
    return listOf(
        UiTableDataColumn(
            text = "Transação",
            width = TableColumnWidth.MaxIntrinsic,
            comparable = { it.viewData.type },
            content = { Text(it.formatted.type) }
        ),
        UiTableDataColumn(
            text = "Data",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.Center,
            comparable = { it.viewData.date },
            content = { Text(it.formatted.date) }
        ),
        UiTableDataColumn(
            text = "Quantidade",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.CenterEnd,
            comparable = { it.viewData.quantity ?: 0.0 },
            content = { Text(it.formatted.quantity ?: "-") }
        ),
        UiTableDataColumn(
            text = "Preço Unitário",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.CenterEnd,
            comparable = { it.viewData.unitPrice ?: 0.0 },
            content = { Text(it.formatted.unitPrice ?: "-") }
        ),
        UiTableDataColumn(
            text = "Valor Total",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.CenterEnd,
            comparable = { it.viewData.totalValue },
            content = { Text(it.formatted.totalValue) }
        ),
    )
}

@Composable
private fun buildFixedIncomeOrFundsColumns(): List<UiTableDataColumn<TransactionRow>> {
    return listOf(
        UiTableDataColumn(
            text = "Transação",
            width = TableColumnWidth.MaxIntrinsic,
            comparable = { it.viewData.type },
            content = { Text(it.formatted.type) }
        ),
        UiTableDataColumn(
            text = "Data",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.Center,
            comparable = { it.viewData.date },
            content = { Text(it.formatted.date) }
        ),
        UiTableDataColumn(
            text = "Valor Total",
            width = TableColumnWidth.MaxIntrinsic,
            alignment = Alignment.CenterEnd,
            comparable = { it.viewData.totalValue },
            content = { Text(it.formatted.totalValue) }
        ),
    )
}
