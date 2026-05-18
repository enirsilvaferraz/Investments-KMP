package com.eferraz.asset_management.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.helpers.FormTextField
import com.eferraz.asset_management.helpers.FormTwoColumnsRow
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage
import org.koin.compose.viewmodel.koinViewModel

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        FormContent(
            ui = ui,
            onEvent = onEvent,
        )

//        Spacer(Modifier.weight(1f))

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

        StableExposedDropdown(
            label = "Categoria",
            displayValue = ui.category.asLabel(),
            options = InvestmentCategory.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.CategoryChanged(it)) },
            enabled = ui.asset == null,
            required = true,
        )

        StableExposedDropdown(
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = ui.issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )

        when (ui.category) {
            InvestmentCategory.FIXED_INCOME -> FixedIncomeFields(ui, onEvent)
            InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFields(ui, onEvent)
            InvestmentCategory.INVESTMENT_FUND -> FundFields(ui, onEvent)
        }

        StableExposedDropdown(
            label = BROKERAGE_FIELD_LABEL,
            displayValue = ui.brokerage?.name.orEmpty(),
            options = ui.brokerages,
            itemLabel = { it.name },
            onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
            error = ui.brokerageError,
            required = true,
        )

        FormTextField(
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
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
            StableExposedDropdown(
                label = "Tipo",
                displayValue = ui.fixedType?.asLabel().orEmpty(),
                options = FixedIncomeAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.FixedTypeChanged(it)) },
                error = ui.fixedTypeError,
                required = true,
            )
        },
        right = {
            StableExposedDropdown(
                label = "Subtipo",
                displayValue = ui.fixedSubType?.asLabel().orEmpty(),
                options = FixedIncomeSubType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.FixedSubTypeChanged(it)) },
                error = ui.fixedSubTypeError,
                required = true,
            )
        },
    )

    FormTwoColumnsRow(
        left = {
            StableExposedDropdown(
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

@Composable
private fun VariableIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            StableExposedDropdown(
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
            FormTextField(
                label = "Identificação",
                value = ui.fundName.orEmpty(),
                onValueChange = { onEvent(AssetManagementEvents.FundNameChanged(it)) },
                errorMessage = ui.fundNameError,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        right = {
            StableExposedDropdown(
                label = "Tipo",
                displayValue = ui.fundType?.asLabel().orEmpty(),
                options = InvestmentFundAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(AssetManagementEvents.FundTypeChanged(it)) },
                error = ui.fundTypeError,
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
        AssetManagementUiState(category = InvestmentCategory.FIXED_INCOME, brokerages = listOf(Brokerage(1L, "XP Investimentos"))),
        AssetManagementUiState(category = InvestmentCategory.VARIABLE_INCOME),
        AssetManagementUiState(category = InvestmentCategory.INVESTMENT_FUND),
    )
}

@Preview
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
