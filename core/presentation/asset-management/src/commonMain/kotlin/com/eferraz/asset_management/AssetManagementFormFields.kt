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

internal fun LazyGridScope.baseForm(
    draft: AssetDraft,
    issuers: List<Issuer>,
    brokerages: List<Brokerage>,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item(span = { GridItemSpan(fullRowSpan) }) {
        StableExposedDropdown(
            label = "Categoria do investimento",
            displayValue = draft.category.asLabel(),
            options = InvestmentCategory.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.CategoryChanged(it)) },
            required = true,
        )
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        StableExposedDropdown(
            label = "Emissor",
            displayValue = draft.issuer?.name.orEmpty(),
            options = issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvent.IssuerChanged(issuer)) },
            error = draft.errors.issuer,
            required = true,
        )
    }

    when (draft.category) {
        InvestmentCategory.FIXED_INCOME -> fixedIncomeFields(draft, onEvent)
        InvestmentCategory.VARIABLE_INCOME -> variableIncomeFields(draft, onEvent)
        InvestmentCategory.INVESTMENT_FUND -> fundFields(draft, onEvent)
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        TextField(
            value = draft.observations.orEmpty(),
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
            displayValue = draft.brokerage?.name.orEmpty(),
            options = brokerages,
            itemLabel = { it.name },
            onItemSelect = { brokerage -> onEvent(AssetManagementEvent.BrokerageChanged(brokerage)) },
            error = draft.errors.brokerage,
            required = true,
        )
    }
}

private fun LazyGridScope.fixedIncomeFields(
    draft: AssetDraft,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo de cálculo",
            displayValue = draft.fixedType?.asLabel().orEmpty(),
            options = FixedIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedTypeChanged(it)) },
            error = draft.errors.fixedType,
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Subtipo do título",
            displayValue = draft.fixedSubType?.asLabel().orEmpty(),
            options = FixedIncomeSubType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedSubTypeChanged(it)) },
            error = draft.errors.fixedSubType,
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Liquidez do título",
            displayValue = draft.fixedLiquidity?.asLabel().orEmpty(),
            options = Liquidity.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FixedLiquidityChanged(it)) },
            error = draft.errors.fixedLiquidity,
            required = true,
        )
    }

    item {
        FormTextField(
            label = "Data de vencimento",
            value = draft.fixedExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvent.FixedExpirationChanged(raw)) },
            errorMessage = draft.errors.fixedExpiration,
            supportingTextWhenNoError = { Text("Obrigatório") },
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
        )
    }

    item {
        FormTextField(
            label = "Rentabilidade (% ao ano)",
            value = draft.fixedYield.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FixedYieldChanged(it)) },
            errorMessage = draft.errors.fixedYield,
            supportingTextWhenNoError = { Text("Obrigatório") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }

    item {
        FormTextField(
            label = "% em relação ao CDI",
            value = draft.fixedCdi.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FixedCdiChanged(it)) },
            errorMessage = draft.errors.fixedCdi,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

private fun LazyGridScope.variableIncomeFields(
    draft: AssetDraft,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo (RV)",
            displayValue = draft.variableType?.asLabel().orEmpty(),
            options = VariableIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.VariableTypeChanged(it)) },
            error = draft.errors.variableType,
        )
    }

    item {
        FormTextField(
            label = "Ticker (código na bolsa)",
            value = draft.variableTicker.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.VariableTickerChanged(it)) },
            errorMessage = draft.errors.variableTicker,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item(span = { GridItemSpan(fullRowSpan) }) {
        FormTextField(
            label = "CNPJ do emissor (opcional)",
            value = draft.variableCnpj.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.VariableCnpjChanged(it)) },
            errorMessage = draft.errors.cnpj,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun LazyGridScope.fundFields(
    draft: AssetDraft,
    onEvent: (AssetManagementEvent) -> Unit,
) {

    item {
        FormTextField(
            label = "Identificação",
            value = draft.fundName.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FundNameChanged(it)) },
            errorMessage = draft.errors.fundName,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        StableExposedDropdown(
            label = "Categoria do fundo",
            displayValue = draft.fundType?.asLabel().orEmpty(),
            options = InvestmentFundAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvent.FundTypeChanged(it)) },
            error = draft.errors.fundType,
        )
    }

    item {
        FormTextField(
            label = "Resgate em (dias úteis)",
            value = draft.fundLiquidityDays.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvent.FundLiquidityDaysChanged(it)) },
            errorMessage = draft.errors.fundLiquidityDays,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        FormTextField(
            label = "Data de vencimento",
            value = draft.fundExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvent.FundExpirationChanged(raw)) },
            errorMessage = draft.errors.fundExpiration,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
