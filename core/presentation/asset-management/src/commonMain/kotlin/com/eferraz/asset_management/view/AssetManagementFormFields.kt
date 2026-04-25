package com.eferraz.asset_management.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

@Composable
internal fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions? = null,
    visualTransformation: VisualTransformation? = null,
    supportingTextWhenNoError: @Composable (() -> Unit)? = null,
) {

    val supportingText = if (errorMessage != null) {
        { Text(errorMessage) }
    } else {
        supportingTextWhenNoError
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
            visualTransformation = visualTransformation ?: VisualTransformation.None,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F5F9),
                unfocusedContainerColor = Color(0xFFF4F5F9),
                disabledContainerColor = Color(0xFFF4F5F9),
                errorContainerColor = Color(0xFFF4F5F9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
internal fun AssetManagementFormContent(
    ui: UiState,
    issuers: List<Issuer>,
    onEvent: (VMEvents) -> Unit,
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
            onItemSelect = { onEvent(VMEvents.CategoryChanged(it)) },
            enabled = ui.editingHoldingId == null,
            required = true,
        )

        StableExposedDropdown(
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(VMEvents.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )

        when (ui.category) {
            InvestmentCategory.FIXED_INCOME -> FixedIncomeFields(ui, onEvent)
            InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFields(ui, onEvent)
            InvestmentCategory.INVESTMENT_FUND -> FundFields(ui, onEvent)
        }

        FormTextField(
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(VMEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )
    }
}

@Composable
internal fun BrokerageField(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    StableExposedDropdown(
        label = BROKERAGE_FIELD_LABEL,
        displayValue = ui.brokerage?.name.orEmpty(),
        options = ui.brokerages,
        itemLabel = Brokerage::name,
        onItemSelect = { brokerage -> onEvent(VMEvents.BrokerageChanged(brokerage)) },
        error = ui.brokerageError,
        required = true,
        modifier = modifier,
    )
}

@Composable
private fun FixedIncomeFields(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            StableExposedDropdown(
                label = "Tipo",
                displayValue = ui.fixedType?.asLabel().orEmpty(),
                options = FixedIncomeAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(VMEvents.FixedTypeChanged(it)) },
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
                onItemSelect = { onEvent(VMEvents.FixedSubTypeChanged(it)) },
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
                onItemSelect = { onEvent(VMEvents.FixedLiquidityChanged(it)) },
                error = ui.fixedLiquidityError,
                required = true,
            )
        },
        right = {
            FormTextField(
                label = "Vencimento",
                value = ui.fixedExpiration.orEmpty(),
                onValueChange = { raw -> onEvent(VMEvents.FixedExpirationChanged(raw)) },
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
                onValueChange = { onEvent(VMEvents.FixedYieldChanged(it)) },
                errorMessage = ui.fixedYieldError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        },
        right = {
            FormTextField(
                label = "Rentabilidade em relação ao CDI",
                value = ui.fixedCdi.orEmpty(),
                onValueChange = { onEvent(VMEvents.FixedCdiChanged(it)) },
                errorMessage = ui.fixedCdiError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        },
    )
}

@Composable
private fun VariableIncomeFields(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            StableExposedDropdown(
                label = "Tipo",
                displayValue = ui.variableType?.asLabel().orEmpty(),
                options = VariableIncomeAssetType.entries.toList(),
                itemLabel = { it.asLabel() },
                onItemSelect = { onEvent(VMEvents.VariableTypeChanged(it)) },
                error = ui.variableTypeError,
            )
        },
        right = {
            FormTextField(
                label = "Ticker",
                value = ui.variableTicker.orEmpty(),
                onValueChange = { onEvent(VMEvents.VariableTickerChanged(it)) },
                errorMessage = ui.variableTickerError,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    FormTextField(
        label = "CNPJ do emissor",
        value = ui.variableCnpj.orEmpty(),
        onValueChange = { onEvent(VMEvents.VariableCnpjChanged(it)) },
        errorMessage = ui.cnpjError,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FundFields(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
) {
    FormTwoColumnsRow(
        left = {
            FormTextField(
                label = "Identificação",
                value = ui.fundName.orEmpty(),
                onValueChange = { onEvent(VMEvents.FundNameChanged(it)) },
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
                onItemSelect = { onEvent(VMEvents.FundTypeChanged(it)) },
                error = ui.fundTypeError,
            )
        },
    )

    FormTwoColumnsRow(
        left = {
            FormTextField(
                label = "Resgate em (dias úteis)",
                value = ui.fundLiquidityDays.orEmpty(),
                onValueChange = { onEvent(VMEvents.FundLiquidityDaysChanged(it)) },
                errorMessage = ui.fundLiquidityDaysError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        right = {
            FormTextField(
                label = "Data de vencimento",
                value = ui.fundExpiration.orEmpty(),
                onValueChange = { raw -> onEvent(VMEvents.FundExpirationChanged(raw)) },
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
private fun FormTwoColumnsRow(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            left()
        }
        Column(modifier = Modifier.weight(1f)) {
            right()
        }
    }
}
