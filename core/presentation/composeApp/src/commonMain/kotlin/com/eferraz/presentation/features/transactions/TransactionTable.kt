package com.eferraz.presentation.features.transactions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.Asset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.TableColumn

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

    DataTable(
        modifier = modifier.fillMaxWidth(),
        columns = columns,
        data = transactions,
    )
}

@Composable
private fun buildVariableIncomeColumns(): List<TableColumn<TransactionRow>> {
    return listOf(
        TableColumn(
            title = "Tipo de Transação",
            data = { viewData.type },
            formated = { formatted.type }
        ),
        TableColumn(
            title = "Data",
            data = { viewData.date },
            formated = { formatted.date },
            alignment = Alignment.CenterHorizontally
        ),
        TableColumn(
            title = "Quantidade",
            data = { viewData.quantity ?: 0.0 },
            formated = { formatted.quantity ?: "-" },
            alignment = Alignment.End
        ),
        TableColumn(
            title = "Preço Unitário",
            data = { viewData.unitPrice ?: 0.0 },
            formated = { formatted.unitPrice ?: "-" },
            alignment = Alignment.End
        ),
        TableColumn(
            title = "Valor Total",
            data = { viewData.totalValue },
            formated = { formatted.totalValue },
            alignment = Alignment.End
        ),
    )
}

@Composable
private fun buildFixedIncomeOrFundsColumns(): List<TableColumn<TransactionRow>> {
    return listOf(
        TableColumn(
            title = "Tipo de Transação",
            data = { viewData.type },
            formated = { formatted.type }
        ),
        TableColumn(
            title = "Data",
            data = { viewData.date },
            formated = { formatted.date },
            alignment = Alignment.CenterHorizontally
        ),
        TableColumn(
            title = "Valor Total",
            data = { viewData.totalValue },
            formated = { formatted.totalValue },
            alignment = Alignment.End
        ),
    )
}

