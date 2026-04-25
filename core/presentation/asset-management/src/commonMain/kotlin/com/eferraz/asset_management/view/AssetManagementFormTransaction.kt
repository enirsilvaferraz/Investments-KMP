package com.eferraz.asset_management.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.components.table.UiTableDataColumn
import com.eferraz.design_system.components.table.UiTableV3
import com.eferraz.design_system.core.StableList
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.entities.assets.InvestmentFundAssetType
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

//        Row (
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ){
//
//            StableExposedDropdown(
//                modifier = Modifier.weight(1f),
//                label = "Transação",
//                displayValue = ui.fundType?.asLabel().orEmpty(),
//                options = InvestmentFundAssetType.entries.toList(),
//                itemLabel = { it.asLabel() },
//                onItemSelect = { onEvent(VMEvents.FundTypeChanged(it)) },
//                error = ui.fundTypeError,
//            )
//
//            FormTextField(
//                modifier = Modifier.width(140.dp),
//                label = "Data",
//                value = ui.fundExpiration.orEmpty(),
//                onValueChange = { raw -> onEvent(VMEvents.FundExpirationChanged(raw)) },
//                errorMessage = ui.fundExpirationError,
//                placeholder = { Text("AAAA-MM-DD", style = MaterialTheme.typography.bodyMedium) },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
//            )
//
//            FormTextField(
//                modifier = Modifier.weight(.5f),
//                label = "Qtde",
//                value = ui.fixedYield.orEmpty(),
//                onValueChange = { onEvent(VMEvents.FixedYieldChanged(it)) },
//                errorMessage = ui.fixedYieldError,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            )
//
//            FormTextField(
//                modifier = Modifier.weight(.7f),
//                label = "Valor",
//                value = ui.fixedYield.orEmpty(),
//                onValueChange = { onEvent(VMEvents.FixedYieldChanged(it)) },
//                errorMessage = ui.fixedYieldError,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//            )
//
//            FilledIconButton(
//                modifier = Modifier.padding(top = 28.dp),
//                onClick = { onEvent(VMEvents.Save) },
//                enabled = !ui.isSaving,
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Salvar"
//                )
//            }
//        }

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