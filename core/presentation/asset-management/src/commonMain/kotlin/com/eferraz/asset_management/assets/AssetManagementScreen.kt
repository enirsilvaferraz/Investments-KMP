package com.eferraz.asset_management.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.FormTextField
import com.eferraz.asset_management.helpers.FormTwoColumnsRow
import com.eferraz.design_system.components.dropdown.AppDropdownField
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.scaffolds.AppContentDialog
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
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
        modifier = modifier.width(800.dp).padding(vertical = 36.dp),
        title = if (holdingId != null) "Editar investimento" else "Novo investimento",
        onDismiss = onDismiss
    ) {

        AssetManagementScreen(
            modifier = Modifier.verticalScroll(rememberScrollState()),
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

    val vm = koinViewModel<AssetManagementViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(holdingId) {
        vm.dispatch(AssetManagementEvents.ScreenEntered(holdingId = holdingId))
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onDismiss()
    }

    AssetFormView(
        modifier = modifier,
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

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {


        FormContent(
            ui = ui,
            onEvent = onEvent,
        )

        Actions(
            ui = ui,
            onEvent = onEvent
        )
    }
}

@Composable
private fun FormContent(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        when (ui.assetClass) {
            AssetClass.FIXED_INCOME -> FixedIncomeFields(ui, onEvent)
            AssetClass.VARIABLE_INCOME -> VariableIncomeFields(ui, onEvent)
            AssetClass.INVESTMENT_FUND -> FundFields(ui, onEvent)
        }

        FormTwoColumnsRow(
            left = {
                AppDropdownField(
                    label = "Emissor",
                    displayValue = ui.issuer?.name.orEmpty(),
                    options = ui.issuers,
                    itemLabel = { it.name },
                    onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
                    error = ui.issuerError,
                    required = true,
                )
            },
            middle = {
                AppDropdownField(
                    label = BROKERAGE_FIELD_LABEL,
                    displayValue = ui.brokerage?.name.orEmpty(),
                    options = ui.brokerages,
                    itemLabel = { it.name },
                    onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
                    error = ui.brokerageError,
                    required = true,
                )
            },
            right = {
                FormTextField(
//            modifier = Modifier.height(200.dp),
                    label = "Observações gerais",
                    value = ui.observations.orEmpty(),
                    onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
                    errorMessage = null,
                )
            }
        )






    }
}

@Composable
private fun FixedIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormTwoColumnsRow(
        left = {
            AppDropdownField(
                label = "Categoria",
                displayValue = ui.assetClass.asLabel(),
                options = AssetClass.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
                enabled = ui.asset == null,
                required = true,
            )
        },
        middle = {
            AppDropdownField(
                label = "Indexador",
                displayValue = ui.yieldIndexer?.asLabel().orEmpty(),
                options = YieldIndexer.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.YieldIndexerChanged(it)) },
                error = ui.yieldIndexerError,
                required = true,
            )
        },
        right = {
            AppDropdownField(
                label = "Tipo",
                displayValue = ui.fixedType?.asLabel().orEmpty(),
                options = FixedIncomeAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.FixedTypeChanged(it)) },
                error = ui.fixedTypeError,
                required = true,
            )
        },
    )

    FormTwoColumnsRow(
        left = {
            FormTwoColumnsRow(
                left = {
                    AppDropdownField(
                        label = "Liquidez",
                        displayValue = ui.fixedLiquidity?.asLabel().orEmpty(),
                        options = Liquidity.entries.toList(),
                        itemLabel = { it.asLabel() },
                        onItemSelect = { onEvent(AssetManagementEvents.FixedLiquidityChanged(it)) },
                        error = ui.fixedLiquidityError,
                        required = true,
                    )
                },
                right = {
                    FormTextField(
                        label = "Vencimento",
                        value = ui.fixedExpiration.orEmpty(),
                        onValueChange = { raw -> onEvent(AssetManagementEvents.FixedExpirationChanged(raw)) },
                        errorMessage = ui.fixedExpirationError,
                        placeholder = { Text("AAAA-MM-DD") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
                    )
                },
            )
        },
        right = {
            FormTwoColumnsRow(
                left = {
                    FormTextField(
                        label = "Rentabilidade (% a.a.)",
                        value = ui.fixedYield.orEmpty(),
                        onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                        errorMessage = ui.fixedYieldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                },
                right = {
                    FormTextField(
                        label = "Rentabilidade (CDI)",
                        value = ui.fixedCdi.orEmpty(),
                        onValueChange = { onEvent(AssetManagementEvents.FixedCdiChanged(it)) },
                        errorMessage = ui.fixedCdiError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                },
            )
        }
    )

    FormTextField(
        label = B3_IDENTIFIER_FIELD_LABEL,
        value = ui.b3Identifier.orEmpty(),
        onValueChange = { onEvent(AssetManagementEvents.B3IdentifierChanged(it)) },
        errorMessage = null,
    )

}

@Composable
private fun VariableIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            AppDropdownField(
                label = "Categoria",
                displayValue = ui.assetClass.asLabel(),
                options = AssetClass.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
                enabled = ui.asset == null,
                required = true,
            )
        },
        middle = {
            AppDropdownField(
                label = "Tipo",
                displayValue = ui.variableType?.asLabel().orEmpty(),
                options = VariableIncomeAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.VariableTypeChanged(it)) },
                error = ui.variableTypeError,
            )
        },
        right = {
            FormTextField(
                label = "Ticker",
                value = ui.variableTicker.orEmpty(),
                onValueChange = { onEvent(AssetManagementEvents.VariableTickerChanged(it)) },
                errorMessage = ui.variableTickerError,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    FormTextField(
        label = "CNPJ do emissor",
        value = ui.variableCnpj.orEmpty(),
        onValueChange = { onEvent(AssetManagementEvents.VariableCnpjChanged(it)) },
        errorMessage = ui.cnpjError,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FundFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            AppDropdownField(
                label = "Categoria",
                displayValue = ui.assetClass.asLabel(),
                options = AssetClass.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
                enabled = ui.asset == null,
                required = true,
            )
        },
        middle = {
            AppDropdownField(
                label = "Tipo",
                displayValue = ui.fundType?.asLabel().orEmpty(),
                options = InvestmentFundAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.FundTypeChanged(it)) },
                error = ui.fundTypeError,
            )
        },
        right = {
            FormTextField(
                label = "Identificação",
                value = ui.fundName.orEmpty(),
                onValueChange = { onEvent(AssetManagementEvents.FundNameChanged(it)) },
                errorMessage = ui.fundNameError,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    FormTwoColumnsRow(
        left = {
            FormTextField(
                label = "Resgate em (dias úteis)",
                value = ui.fundLiquidityDays.orEmpty(),
                onValueChange = { onEvent(AssetManagementEvents.FundLiquidityDaysChanged(it)) },
                errorMessage = ui.fundLiquidityDaysError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        right = {
            FormTextField(
                label = "Data de vencimento",
                value = ui.fundExpiration.orEmpty(),
                onValueChange = { raw -> onEvent(AssetManagementEvents.FundExpirationChanged(raw)) },
                errorMessage = ui.fundExpirationError,
                placeholder = { Text("AAAA-MM-DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onEvent: (AssetManagementEvents) -> Unit,
    ui: AssetManagementUiState,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.Bottom,
    ) {

        Button(
            onClick = { onEvent(AssetManagementEvents.Save) },
            enabled = !ui.isSaving,
        ) {
            Text("Salvar")
        }
    }
}

private class AssetFormPreviewProvider : PreviewParameterProvider<AssetManagementUiState> {
    override val values: Sequence<AssetManagementUiState> = sequenceOf(
        AssetManagementUiState(assetClass = AssetClass.FIXED_INCOME, brokerages = listOf(Brokerage(1L, "XP Investimentos"))),
        AssetManagementUiState(assetClass = AssetClass.VARIABLE_INCOME),
        AssetManagementUiState(assetClass = AssetClass.INVESTMENT_FUND),
    )
}

@Preview(device = Devices.TABLET)
@Composable
private fun AssetManagementScreenPreview(
    @PreviewParameter(AssetFormPreviewProvider::class) ui: AssetManagementUiState,
) {
    MaterialTheme {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Novo investimento") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    },
                )
            }
        ) {
            AssetFormView(
                modifier = Modifier.padding(it),
                ui = ui,
                onEvent = {}
            )
        }
    }
}
