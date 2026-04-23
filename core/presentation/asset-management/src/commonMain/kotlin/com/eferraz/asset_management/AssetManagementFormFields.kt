package com.eferraz.asset_management

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.helpers.asLabel
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
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder,
        modifier = modifier,
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            { Text(errorMessage) }
        } else {
            supportingTextWhenNoError
        },
        keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
        visualTransformation = visualTransformation ?: VisualTransformation.None,
    )
}

private const val fullRowSpan = 2

internal fun LazyGridScope.assetManagementForm(
    ui: UiState,
    issuers: List<Issuer>,
    brokerages: List<Brokerage>,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item(span = { GridItemSpan(fullRowSpan) }) {
        StableExposedDropdown(
            label = "Categoria do investimento",
            displayValue = ui.category.asLabel(),
            options = InvestmentCategory.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.CategoryChanged(it)) },
            required = true,
        )
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        StableExposedDropdown(
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvent.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )
    }

    when (ui.category) {
        InvestmentCategory.FIXED_INCOME -> fixedIncomeFields(ui, onEvent)
        InvestmentCategory.VARIABLE_INCOME -> variableIncomeFields(ui, onEvent)
        InvestmentCategory.INVESTMENT_FUND -> fundFields(ui, onEvent)
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        TextField(
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.ObservationsChanged(it)) },
            label = { Text("Observações gerais") },
            placeholder = { Text("Ex.: estratégia, lembretes, ISIN…") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        StableExposedDropdown(
            label = BROKERAGE_FIELD_LABEL,
            displayValue = ui.brokerage?.name.orEmpty(),
            options = brokerages,
            itemLabel = { it.name },
            onItemSelect = { brokerage -> onEvent(AssetManagementEvent.BrokerageChanged(brokerage)) },
            error = ui.brokerageError,
            required = true,
        )
    }
}

private fun LazyGridScope.fixedIncomeFields(
    ui: UiState,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo de cálculo",
            displayValue = ui.fixedType?.asLabel().orEmpty(),
            options = FixedIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedTypeChanged(it)) },
            error = ui.fixedTypeError,
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Subtipo do título",
            displayValue = ui.fixedSubType?.asLabel().orEmpty(),
            options = FixedIncomeSubType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedSubTypeChanged(it)) },
            error = ui.fixedSubTypeError,
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Liquidez do título",
            displayValue = ui.fixedLiquidity?.asLabel().orEmpty(),
            options = Liquidity.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedLiquidityChanged(it)) },
            error = ui.fixedLiquidityError,
            required = true,
        )
    }

    item {
        FormTextField(
            label = "Data de vencimento",
            value = ui.fixedExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvent.FixedExpirationChanged(raw)) },
            errorMessage = ui.fixedExpirationError,
            supportingTextWhenNoError = { Text("Obrigatório") },
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
        )
    }

    item {
        FormTextField(
            label = "Rentabilidade (% ao ano)",
            value = ui.fixedYield.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FixedYieldChanged(it)) },
            errorMessage = ui.fixedYieldError,
            supportingTextWhenNoError = { Text("Obrigatório") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }

    item {
        FormTextField(
            label = "% em relação ao CDI",
            value = ui.fixedCdi.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FixedCdiChanged(it)) },
            errorMessage = ui.fixedCdiError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

private fun LazyGridScope.variableIncomeFields(
    ui: UiState,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo (RV)",
            displayValue = ui.variableType?.asLabel().orEmpty(),
            options = VariableIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.VariableTypeChanged(it)) },
            error = ui.variableTypeError,
        )
    }

    item {
        FormTextField(
            label = "Ticker (código na bolsa)",
            value = ui.variableTicker.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.VariableTickerChanged(it)) },
            errorMessage = ui.variableTickerError,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        FormTextField(
            label = "CNPJ do emissor (opcional)",
            value = ui.variableCnpj.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.VariableCnpjChanged(it)) },
            errorMessage = ui.cnpjError,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun LazyGridScope.fundFields(
    ui: UiState,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        FormTextField(
            label = "Identificação",
            value = ui.fundName.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FundNameChanged(it)) },
            errorMessage = ui.fundNameError,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        StableExposedDropdown(
            label = "Categoria do fundo",
            displayValue = ui.fundType?.asLabel().orEmpty(),
            options = InvestmentFundAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FundTypeChanged(it)) },
            error = ui.fundTypeError,
        )
    }

    item {
        FormTextField(
            label = "Resgate em (dias úteis)",
            value = ui.fundLiquidityDays.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FundLiquidityDaysChanged(it)) },
            errorMessage = ui.fundLiquidityDaysError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        FormTextField(
            label = "Data de vencimento",
            value = ui.fundExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvent.FundExpirationChanged(raw)) },
            errorMessage = ui.fundExpirationError,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
