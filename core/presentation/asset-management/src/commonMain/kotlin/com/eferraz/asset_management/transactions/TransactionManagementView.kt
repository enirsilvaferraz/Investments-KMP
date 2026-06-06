package com.eferraz.asset_management.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.assets.TransactionDraftUi
import com.eferraz.asset_management.helpers.FormTextField
import com.eferraz.design_system.components.dropdown.AppDropdownField
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.scaffolds.AppContentDialog
import com.eferraz.design_system.theme.AppTheme
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.naming.asLabel

@Composable
internal fun TransactionFormContent(
    transactions: List<TransactionDraftUi>,
    assetClass: AssetClass,
    onAdd: () -> Unit,
    onRemove: (index: Int) -> Unit,
    onDateChanged: (index: Int, digits: String) -> Unit,
    onTypeChanged: (index: Int, type: TransactionType) -> Unit,
    onQuantityChanged: (index: Int, value: String) -> Unit,
    onUnitPriceChanged: (index: Int, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVariableIncome = assetClass == AssetClass.VARIABLE_INCOME

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TransactionHeader()

            TransactionTable(
                transactions = transactions,
                isVariableIncome = isVariableIncome,
                onRemove = onRemove,
                onDateChanged = onDateChanged,
                onTypeChanged = onTypeChanged,
                onQuantityChanged = onQuantityChanged,
                onUnitPriceChanged = onUnitPriceChanged,
            )

            FilledTonalButton(
                modifier = Modifier.padding(top = 0.dp).width(135.dp),
                onClick = onAdd,
            ) {
                Text("Adicionar")
            }
        }
    }
}

@Composable
private fun TransactionHeader() {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val clip = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .height(32.dp)

        Box(modifier = clip.width(135.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Data",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = clip.width(140.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Transação",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = clip.weight(0.5f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Qtde",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = clip.weight(1.1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Valor Unit.",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = clip.weight(1.1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Valor Total",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun TransactionTable(
    transactions: List<TransactionDraftUi>,
    isVariableIncome: Boolean,
    onRemove: (index: Int) -> Unit,
    onDateChanged: (index: Int, digits: String) -> Unit,
    onTypeChanged: (index: Int, type: TransactionType) -> Unit,
    onQuantityChanged: (index: Int, value: String) -> Unit,
    onUnitPriceChanged: (index: Int, value: String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        transactions.forEachIndexed { index, draft ->
            TransactionTableRow(
                draft = draft,
                index = index,
                isVariableIncome = isVariableIncome,
                onRemove = onRemove,
                onDateChanged = onDateChanged,
                onTypeChanged = onTypeChanged,
                onQuantityChanged = onQuantityChanged,
                onUnitPriceChanged = onUnitPriceChanged,
            )
        }
    }
}

@Composable
private fun TransactionTableRow(
    draft: TransactionDraftUi,
    index: Int,
    isVariableIncome: Boolean,
    onRemove: (index: Int) -> Unit,
    onDateChanged: (index: Int, digits: String) -> Unit,
    onTypeChanged: (index: Int, type: TransactionType) -> Unit,
    onQuantityChanged: (index: Int, value: String) -> Unit,
    onUnitPriceChanged: (index: Int, value: String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormTextField(
            modifier = Modifier.width(135.dp),
            label = "",
            value = draft.dateDigits,
            onValueChange = { raw -> onDateChanged(index, raw) },
            errorMessage = null,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
        )

        AppDropdownField(
            modifier = Modifier.width(140.dp),
            label = "",
            displayValue = draft.type.asLabel(),
            options = TransactionType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onTypeChanged(index, it) },
        )

        FormTextField(
            modifier = Modifier.weight(0.5f),
            label = "",
            value = draft.quantity,
            onValueChange = { onQuantityChanged(index, it) },
            errorMessage = null,
            readOnly = !isVariableIncome,
            keyboardOptions = if (isVariableIncome) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            },
        )

        FormTextField(
            modifier = Modifier.weight(1.1f),
            label = "",
            value = draft.unitPrice,
            onValueChange = { onUnitPriceChanged(index, it) },
            errorMessage = null,
        )

        FormTextField(
            modifier = Modifier.weight(1.1f),
            label = "",
            value = draft.totalValue,
            onValueChange = {},
            errorMessage = null,
            readOnly = true,
        )

        FilledIconButton(
            onClick = { onRemove(index) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remover",
            )
        }
    }
}

private class TransactionFormPreviewProvider : PreviewParameterProvider<List<TransactionDraftUi>> {

    override val values: Sequence<List<TransactionDraftUi>> = sequenceOf(
        listOf(
            TransactionDraftUi(
                assetClass = AssetClass.FIXED_INCOME,
                dateDigits = "20250110",
                type = TransactionType.PURCHASE,
                quantity = "1",
                unitPrice = "5000.00",
                totalValue = "5000.00",
            ),
            TransactionDraftUi(
                assetClass = AssetClass.FIXED_INCOME,
                dateDigits = "20250215",
                type = TransactionType.PURCHASE,
                quantity = "1",
                unitPrice = "3500.00",
                totalValue = "3500.00",
            ),
        ),
        listOf(
            TransactionDraftUi(
                assetClass = AssetClass.INVESTMENT_FUND,
                dateDigits = "20250301",
                type = TransactionType.PURCHASE,
                quantity = "1",
                unitPrice = "5000.00",
                totalValue = "5000.00",
            ),
        ),
        listOf(
            TransactionDraftUi(
                assetClass = AssetClass.VARIABLE_INCOME,
                dateDigits = "20250110",
                type = TransactionType.PURCHASE,
                quantity = "100",
                unitPrice = "28.50",
                totalValue = "2850.0",
            ),
        ),
        emptyList(),
    )
}

@Preview(widthDp = 800)
@Composable
private fun TransactionFormContentPreview(
    @PreviewParameter(TransactionFormPreviewProvider::class) transactions: List<TransactionDraftUi>,
) {
    AppTheme {
        AppContentDialog(
            modifier = Modifier.width(800.dp).padding(vertical = 0.dp),
            title = "Transações",
            onDismiss = {},
        ) {
            TransactionFormContent(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                transactions = transactions,
                assetClass = transactions.firstOrNull()?.assetClass ?: AssetClass.FIXED_INCOME,
                onAdd = {},
                onRemove = {},
                onDateChanged = { _, _ -> },
                onTypeChanged = { _, _ -> },
                onQuantityChanged = { _, _ -> },
                onUnitPriceChanged = { _, _ -> },
            )
        }
    }
}
