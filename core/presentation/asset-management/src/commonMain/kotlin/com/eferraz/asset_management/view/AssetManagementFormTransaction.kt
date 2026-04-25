package com.eferraz.asset_management.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.entities.transactions.AssetTransaction
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

        BrokerageField(ui = ui, onEvent = onEvent)

        Text(
            text = "Transações",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        TransactionTable(
            modifier = Modifier.fillMaxWidth(),
            transactions = ui.transactions,
        )
    }
}

@Composable
internal fun TransactionTable(
    modifier: Modifier = Modifier,
    transactions: List<AssetTransaction>,
) {
    val headerAndFooterColor = Color(0xFFE6DBF7)

    UiTableV3(
        modifier = modifier.fillMaxWidth(),
        columns = StableList(
            listOf(
                UiTableDataColumn(
                    text = "Data",
                    comparable = { it.date },
                    content = { Text(it.formattedDate()) },
                ),
                UiTableDataColumn(
                    text = "Transação",
                    comparable = { it.type },
                    content = { Text(it.typeLabel()) },
                ),
                UiTableDataColumn(
                    text = "Valor Total",
                    alignment = Alignment.CenterEnd,
                    comparable = { it.totalValue },
                    content = { Text(it.totalValue.toCurrencyBr()) },
                ),
            )
        ),
        rows = StableList(transactions),
        headerBackgroundColor = headerAndFooterColor,
        footerBackgroundColor = headerAndFooterColor,
        rowBackgroundColor = Color(0xFFF4F5F9),
    )
}

@Deprecated("Transformar em ui-commons")
private fun AssetTransaction.typeLabel(): String =
    when (this.type.name) {
        "PURCHASE" -> "Compra"
        "SALE" -> "Venda"
        else -> this.type.name
    }

@Deprecated("Transformar em ui-commons")
private fun AssetTransaction.formattedDate(): String {
    val d = this.date
    val day = d.dayOfMonth.toString().padStart(2, '0')
    val month = d.monthNumber.toString().padStart(2, '0')
    val year = d.year.toString()
    return "$year.$month.$day"
}

@Deprecated("Transformar em ui-commons")
private fun Double.toCurrencyBr(): String {
    val isNegative = this < 0
    val absoluteCents = (this.absoluteValue * 100).roundToInt()
    val integerPart = absoluteCents / 100
    val decimalPart = absoluteCents % 100

    val grouped = integerPart
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    val sign = if (isNegative) "-" else ""
    return "${sign}R$ $grouped,${decimalPart.toString().padStart(2, '0')}"
}