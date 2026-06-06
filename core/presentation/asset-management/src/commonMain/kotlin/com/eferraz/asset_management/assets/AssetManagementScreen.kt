package com.eferraz.asset_management.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.FormTextField
import kotlinx.coroutines.flow.first
import com.eferraz.asset_management.transactions.TransactionFormContent
import com.eferraz.design_system.components.dropdown.AppDropdownField
import com.eferraz.design_system.components.segmented_control.SegmentedControl
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.scaffolds.AppContentDialog
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.naming.B3_IDENTIFIER_FIELD_LABEL
import com.eferraz.naming.BROKERAGE_FIELD_LABEL
import com.eferraz.naming.asLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun AssetManagementDialog(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onDismiss: () -> Unit,
) {

    AppContentDialog(
        modifier = modifier.width(1000.dp).padding(vertical = 36.dp),
        title = if (holdingId != null) "Editar investimento" else "Novo investimento",
        leadingIcon = Icons.Outlined.Info,
        onDismiss = onDismiss
    ) {

        AssetManagementScreen(
            modifier = Modifier,
            holdingId = holdingId,
            onDismiss = onDismiss,
        )
    }
}

@Composable
public fun AssetManagementScreen(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onDismiss: () -> Unit,
) {

    val vm = koinViewModel<AssetManagementViewModel>(
        key = "asset-management-${holdingId ?: "new"}",
    )
    val state by vm.state.collectAsState()

    LaunchedEffect(holdingId) {
        vm.dispatch(AssetManagementEvents.ScreenEntered(holdingId = holdingId))
        vm.dismissAfterSave.first()
        onDismiss()
    }

    AssetFormView(
        modifier = modifier.verticalScroll(rememberScrollState()),
        ui = state,
        onEvent = vm::dispatch,
    )
}

@Composable
private fun AssetFormView(
    modifier: Modifier = Modifier,
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    Column(modifier) {

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            FormSection(
                title = "ATIVO",
                icon = Icons.Outlined.AttachMoney
            ) {

                FormRow {

                    AppDropdownField(
                        modifier = Modifier.weight(1f),
                        label = "Classe do Ativo",
                        displayValue = ui.assetClass.asLabel(),
                        options = AssetClass.entries.toList(),
                        itemLabel = { it.asLabel() },
                        onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
                        enabled = ui.asset == null,
                        required = true,
                    )

                    AppDropdownField(
                        modifier = Modifier.weight(0.9f),
                        label = "Tipo",
                        displayValue = ui.type?.asLabel().orEmpty(),
                        options = ui.assetTypeOptions,
                        itemLabel = { it.asLabel() },
                        onItemSelect = { onEvent(AssetManagementEvents.TypeChanged(it)) },
                        error = ui.typeError,
                        required = true,
                    )

                    AppDropdownField(
                        modifier = Modifier.weight(1f),
                        label = "Emissor",
                        displayValue = ui.issuer?.name.orEmpty(),
                        options = ui.issuers,
                        itemLabel = { it.name },
                        onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
                        error = ui.issuerError,
                        required = true,
                    )
                }

                when (ui.assetClass) {
                    AssetClass.FIXED_INCOME -> FixedIncomeFields(ui, onEvent)
                    AssetClass.VARIABLE_INCOME -> VariableIncomeFields(ui, onEvent)
                    AssetClass.INVESTMENT_FUND -> FundFields(ui, onEvent)
                }
            }

            FormSection(
                title = "POSICIONAMENTO",
                icon = Icons.Outlined.Home
            ) {

                FormRow {

                    FormTextField(
                        modifier = Modifier.width(250.dp),
                        label = "Titular",
                        value = ui.owner?.name.orEmpty(),
                        onValueChange = {},
                        errorMessage = null,
                        readOnly = true,
                    )

                    AppDropdownField(
                        modifier = Modifier.weight(1f),
                        label = BROKERAGE_FIELD_LABEL,
                        displayValue = ui.brokerage?.name.orEmpty(),
                        options = ui.brokerages,
                        itemLabel = { it.name },
                        onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
                        error = ui.brokerageError,
                        required = true,
                    )
                }
            }

            FormSection(
                title = "TRANSAÇÕES",
                icon = Icons.Outlined.SwapVert
            ) {

                TransactionFormContent(
                    transactions = ui.transactions,
                    assetClass = ui.assetClass,
                    onAdd = { onEvent(AssetManagementEvents.TransactionAdded(ui.assetClass)) },
                    onRemove = { index -> onEvent(AssetManagementEvents.TransactionRemoved(index)) },
                    onDateChanged = { index, digits ->
                        onEvent(AssetManagementEvents.TransactionDateChanged(index, digits))
                    },
                    onTypeChanged = { index, type ->
                        onEvent(AssetManagementEvents.TransactionTypeChanged(index, type))
                    },
                    onQuantityChanged = { index, value ->
                        onEvent(AssetManagementEvents.TransactionQuantityChanged(index, value))
                    },
                    onUnitPriceChanged = { index, value ->
                        onEvent(AssetManagementEvents.TransactionUnitPriceChanged(index, value))
                    },
                )
            }

            // TODO Resumo sera adicionado em uma feature futura, manter formulario para testes
            if (false) FormSection(
                title = "RESUMO",
                icon = Icons.Outlined.Summarize
            ) {

                FormRow {

                    FormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Balanço das Transações",
                        value = "1.000,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )

                    FormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Valor Atual (Bruto)",
                        value = "1.000,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )

                    FormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Lucro Bruto",
                        value = "1.000,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )
                }

                FormRow {

                    FormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Impostos",
                        value = "100,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )

                    FormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Total Líquido",
                        value = "1.000,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )

                    FormTextField(
                        modifier = Modifier.weight(.6f),
                        label = "Lucro Líquido",
                        value = "1.000,00",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        prefix = { Text("R$ ") }
                    )

                    FormTextField(
                        modifier = Modifier.weight(.35f),
                        label = " ",
                        value = "22,5",
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        readOnly = true,
                        suffix = { Text("%") }
                    )
                }
            }
        }

        Surface {

            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Button(
                    onClick = { /* TODO NOT IMPLEMENTED YET */ },
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Excluir")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ui.saveError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }

                FilledTonalButton(
                    onClick = { onEvent(AssetManagementEvents.Save) },
                    enabled = !ui.isSaving,
                ) {
                    Text("Salvar")
                }
            }
        }
    }


}

@Composable
private fun FormSection(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {

    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
        ) {

            FormHeader(
                title = title,
                icon = icon
            )

            Column(
                modifier = Modifier.padding(top = 24.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun FormRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {

    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun FormHeader(
    title: String,
    icon: ImageVector,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = DividerDefaults.color.copy(alpha = 0.4f))
}


@Composable
private fun FixedIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        AppDropdownField(
            modifier = Modifier.weight(.35f),
            label = "Liquidez",
            displayValue = ui.fixedLiquidity?.asLabel().orEmpty(),
            options = Liquidity.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.FixedLiquidityChanged(it)) },
            error = ui.fixedLiquidityError,
            required = true,
        )

        FormTextField(
            modifier = Modifier.weight(.35f),
            label = "Vencimento",
            value = ui.fixedExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvents.FixedExpirationChanged(raw)) },
            errorMessage = ui.fixedExpirationError,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            trailingIcon = {
                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null)
            }
        )

        AppDropdownField(
            modifier = Modifier.weight(.45f),
            label = "Indexador",
            displayValue = ui.yieldIndexer?.asLabel().orEmpty(),
            options = YieldIndexer.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.YieldIndexerChanged(it)) },
            error = ui.yieldIndexerError,
            required = true,
        )

        FormTextField(
            modifier = Modifier.weight(.2f),
            label = "Rent. (a.a.)",
            value = ui.fixedYield.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
            errorMessage = ui.fixedYieldError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("%") }
        )

        FormTextField(
            modifier = Modifier.weight(.2f),
            label = "Rent. (CDI)",
            value = ui.fixedCdi.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FixedCdiChanged(it)) },
            errorMessage = ui.fixedCdiError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("%") }
        )
    }

    FormRow {

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )

        FormTextField(
            modifier = Modifier.weight(.4f),
            label = B3_IDENTIFIER_FIELD_LABEL,
            value = ui.b3Identifier.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.B3IdentifierChanged(it)) },
            errorMessage = null,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            }
        )

        Column(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            "Isento de IR".takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            SegmentedControl(
                modifier = Modifier.height(55.dp),
                selected = ui.incomeTaxSelected,
                options = incomeTaxSegmentOptions,
                onSelect = { choice ->
                    onEvent(AssetManagementEvents.IncomeTaxExemptChanged(choice.label == "Sim"))
                },
            )
        }
    }
}

@Composable
private fun VariableIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )

        FormTextField(
            modifier = Modifier.weight(.4f),
            label = "Ticker",
            value = ui.variableTicker.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.VariableTickerChanged(it)) },
            errorMessage = ui.variableTickerError,
        )
    }
}

@Composable
private fun FundFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        FormTextField(
            label = "Identificação",
            value = ui.fundName.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FundNameChanged(it)) },
            errorMessage = ui.fundNameError,
            modifier = Modifier.weight(1f),
        )

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )
    }
}

private class AssetFormPreviewProvider : PreviewParameterProvider<AssetManagementUiState> {

    override val values: Sequence<AssetManagementUiState> = sequenceOf(
        AssetManagementUiState(assetClass = AssetClass.FIXED_INCOME, brokerages = listOf(Brokerage(1L, "XP Investimentos"))),
        AssetManagementUiState(assetClass = AssetClass.VARIABLE_INCOME),
        AssetManagementUiState(assetClass = AssetClass.INVESTMENT_FUND),
    )

    override fun getDisplayName(index: Int) = when (index) {
        0 -> "Fixed Income"
        1 -> "Variable Income"
        2 -> "Investment Fund"
        else -> null
    }
}

@Preview(widthDp = 1000, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(widthDp = 1000, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AssetManagementScreenPreview(
    @PreviewParameter(AssetFormPreviewProvider::class) ui: AssetManagementUiState,
) {

    AppThemeV2 {

        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {

            AppContentDialog(
                modifier = Modifier.width(800.dp).padding(16.dp),
                title = "Novo investimento",
                onDismiss = {}
            ) {

                AssetFormView(
                    modifier = Modifier,
                    ui = ui,
                    onEvent = {}
                )
            }
        }
    }
}
