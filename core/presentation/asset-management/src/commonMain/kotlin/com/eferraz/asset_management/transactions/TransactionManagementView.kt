package com.eferraz.asset_management.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.FormTextField
import com.eferraz.design_system.components.dropdown.AppDropdownField
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.scaffolds.AppContentDialog
import com.eferraz.design_system.theme.AppTheme
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.naming.asLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun TransactionFormDialog(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onDismiss: () -> Unit,
) {

    AppContentDialog(
        modifier = modifier.width(800.dp).padding(vertical = 32.dp),
        title = "Transações",
        onDismiss = onDismiss,
    ) {


        TransactionFormView(
            modifier = Modifier,
//            .verticalScroll(rememberScrollState())
//            .padding(16.dp),
            holdingId = holdingId,
            onComplete = onDismiss,
        )

    }
}

@Composable
public fun TransactionFormView(
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

    TransactionFormContent(
        modifier = modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
        state = state,
        onEvent = vm::dispatch,
    )
}

@Composable
private fun TransactionFormContent(
    modifier: Modifier = Modifier,
    state: TransactionManagementUiState,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {

            Column(
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                TransactionHeader()

                TransactionTable(
                    state = state,
                    onEvent = onEvent,
                )

                FilledTonalButton(
                    modifier = Modifier.padding(top = 0.dp).width(135.dp),
                    onClick = { onEvent(TransactionManagementEvents.AddTransactionDraft) },
                ) {
                    Text("Adicionar")
                }
            }
        }

        TransactionFormActions(
            state = state,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun TransactionHeader() {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val clip = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiaryContainer).height(32.dp)

        Box(modifier = clip.width(135.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Data",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
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
private fun TransactionFormActions(
    modifier: Modifier = Modifier,
    state: TransactionManagementUiState,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Button(
            onClick = { onEvent(TransactionManagementEvents.Save) },
            enabled = state.isDirty && !state.isSaving,
        ) {
            Text("Salvar")
        }
    }
}

@Composable
private fun TransactionTable(
    modifier: Modifier = Modifier,
    state: TransactionManagementUiState,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        state.transactions.forEachIndexed { index, draft ->
            TransactionTableRow(
                draft = draft,
                index = index,
                isVariableIncome = state.category == InvestmentCategory.VARIABLE_INCOME,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun TransactionTableRow(
    draft: TransactionDraftUi,
    index: Int,
    isVariableIncome: Boolean,
    onEvent: (TransactionManagementEvents) -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        FormTextField(
            modifier = Modifier.width(135.dp),
            label = "", // if (index != 0) "" else "Data",
            value = draft.dateDigits,
            onValueChange = { raw -> onEvent(TransactionManagementEvents.DraftTransactionDateChanged(index, raw)) },
            errorMessage = null, //if (draft.dateError) "Inválido" else null,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
        )

        AppDropdownField(
            modifier = Modifier.width(140.dp),
            label = "", // if (index != 0) "" else "Transação",
            displayValue = draft.type.asLabel(),
            options = TransactionType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(TransactionManagementEvents.DraftTransactionTypeChanged(index, it)) },
        )

        FormTextField(
            readOnly = !isVariableIncome,
            modifier = Modifier.weight(0.5f),
            label = "", //  if (index != 0) "" else "Qtde",
            value = draft.quantity,
            onValueChange = { onEvent(TransactionManagementEvents.DraftTransactionQuantityChanged(index, it)) },
            errorMessage = null, //if (draft.quantityError) "Inválido" else null,
        )

        FormTextField(
            readOnly = !isVariableIncome,
            modifier = Modifier.weight(1.1f),
            label = "", // if (index != 0) "" else "Valor Unit.",
            value = draft.unitPrice,
            onValueChange = { onEvent(TransactionManagementEvents.DraftTransactionUnitPriceChanged(index, it)) },
            errorMessage = null, //if (draft.unitPriceError) "Inválido" else null,
        )

        FormTextField(
            modifier = Modifier.weight(1.1f),
            label = "", // if (index != 0) "" else "Valor Total",
            value = draft.totalValue,
            onValueChange = { onEvent(TransactionManagementEvents.DraftTransactionTotalValueChanged(index, it)) },
            errorMessage = null, //if (draft.totalValueError) "Inválido" else null,
            readOnly = isVariableIncome,
        )

        FilledIconButton(
//            modifier = if (index != 0) Modifier else Modifier.padding(top = 26.dp),
            onClick = { onEvent(TransactionManagementEvents.DraftTransactionDeleteClicked(index)) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer,
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remover",
            )
        }
    }
}

private class TransactionFormPreviewProvider : PreviewParameterProvider<TransactionManagementUiState> {

    private fun previewState(
        transactions: List<TransactionDraftUi>,
    ): TransactionManagementUiState =
        TransactionManagementUiState(
            transactions = transactions,
            initialSnapshot = transactions,
        )

    override val values: Sequence<TransactionManagementUiState> = sequenceOf(
        previewState(
            listOf(
                TransactionDraftUi(
                    category = InvestmentCategory.FIXED_INCOME,
                    dateDigits = "20250110",
                    type = TransactionType.PURCHASE,
                    totalValue = "5000.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.FIXED_INCOME,
                    dateDigits = "20250215",
                    type = TransactionType.PURCHASE,
                    totalValue = "3500.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.FIXED_INCOME,
                    dateDigits = "20250320",
                    type = TransactionType.SALE,
                    totalValue = "1200.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.FIXED_INCOME,
                    dateDigits = "20250401",
                    type = TransactionType.PURCHASE,
                    totalValue = "8000.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.FIXED_INCOME,
                    dateDigits = "20250510",
                    type = TransactionType.SALE,
                    totalValue = "2700.00"
                ),
            ),
        ),
        previewState(
            listOf(
                TransactionDraftUi(
                    category = InvestmentCategory.VARIABLE_INCOME,
                    dateDigits = "20250110",
                    type = TransactionType.PURCHASE,
                    quantity = "100",
                    unitPrice = "28.50",
                    totalValue = "2850.0"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.VARIABLE_INCOME,
                    dateDigits = "20250202",
                    type = TransactionType.PURCHASE,
                    quantity = "50",
                    unitPrice = "31.20",
                    totalValue = "1560.0"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.VARIABLE_INCOME,
                    dateDigits = "20250315",
                    type = TransactionType.SALE,
                    quantity = "30",
                    unitPrice = "34.00",
                    totalValue = "1020.0"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.VARIABLE_INCOME,
                    dateDigits = "20250420",
                    type = TransactionType.PURCHASE,
                    quantity = "200",
                    unitPrice = "29.75",
                    totalValue = "5950.0"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.VARIABLE_INCOME,
                    dateDigits = "20250505",
                    type = TransactionType.SALE,
                    quantity = "80",
                    unitPrice = "33.10",
                    totalValue = "2648.0"
                ),
            ),
        ),
        previewState(
            listOf(
                TransactionDraftUi(
                    category = InvestmentCategory.INVESTMENT_FUND,
                    dateDigits = "20250110",
                    type = TransactionType.PURCHASE,
                    totalValue = "10000.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.INVESTMENT_FUND,
                    dateDigits = "20250210",
                    type = TransactionType.PURCHASE,
                    totalValue = "5000.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.INVESTMENT_FUND,
                    dateDigits = "20250312",
                    type = TransactionType.SALE,
                    totalValue = "3000.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.INVESTMENT_FUND,
                    dateDigits = "20250415",
                    type = TransactionType.PURCHASE,
                    totalValue = "7500.00"
                ),
                TransactionDraftUi(
                    category = InvestmentCategory.INVESTMENT_FUND,
                    dateDigits = "20250520",
                    type = TransactionType.SALE,
                    totalValue = "4200.00"
                ),
            ),
        ),
        TransactionManagementUiState(),
    )
}

@Preview(widthDp = 800)
@Composable
private fun TransactionFormViewPreview(
    @PreviewParameter(TransactionFormPreviewProvider::class) ui: TransactionManagementUiState,
) {
    AppTheme {

        AppContentDialog(
            modifier = Modifier.width(800.dp).padding(vertical = 0.dp),
            title = "Transações",
            onDismiss = { },
        ) {

            TransactionFormContent(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                state = ui,
                onEvent = {},
            )
        }
    }
}
