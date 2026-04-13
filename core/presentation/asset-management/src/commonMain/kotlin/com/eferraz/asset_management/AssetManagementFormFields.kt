package com.eferraz.asset_management

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.core.StableMap
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.input.date.filterDateMaskDigits
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

internal fun LazyGridScope.baseForm(
    draft: AssetDraft,
    onCategoryChange: (InvestmentCategory) -> Unit,
    issuers: List<Issuer>,
    brokerages: List<Brokerage>,
    onDraftChange: (AssetDraft) -> Unit,
    fieldErrors: StableMap<String, String>,
) {

    item(span = { GridItemSpan(maxLineSpan) }) {
        StableExposedDropdown(
            label = "Categoria do investimento",
            displayValue = draft.category.asLabel(),
            options = InvestmentCategory.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = onCategoryChange,
            required = true,
        )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        StableExposedDropdown(
            label = "Emissor",
            displayValue = issuers.find { it.id == draft.issuerId }?.name.orEmpty(),
            options = issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onDraftChange(draft.copy(issuerId = issuer.id)) },
            error = fieldErrors["issuer"],
            required = true,
        )
    }

    when (draft.category) {
        InvestmentCategory.FIXED_INCOME -> fixedIncomeFields(draft, fieldErrors, onDraftChange)
        InvestmentCategory.VARIABLE_INCOME -> variableIncomeFields(draft, fieldErrors, onDraftChange)
        InvestmentCategory.INVESTMENT_FUND -> fundFields(draft, fieldErrors, onDraftChange)
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        TextField(
            value = draft.observations.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(observations = it)) },
            label = { Text("Observações gerais") },
            placeholder = { Text("Ex.: estratégia, lembretes, ISIN…") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        StableExposedDropdown(
            label = BROKERAGE_FIELD_LABEL,
            displayValue = brokerages.find { it.id == draft.brokerageId }?.name.orEmpty(),
            options = brokerages,
            itemLabel = { it.name },
            onItemSelect = { brokerage -> onDraftChange(draft.copy(brokerageId = brokerage.id)) },
            error = fieldErrors["brokerage"],
            required = true,
        )
    }
}

private fun LazyGridScope.fixedIncomeFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo de cálculo",
            displayValue = draft.fixedType?.asLabel().orEmpty(),
            options = FixedIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fixedType = it)) },
            error = fieldErrors["fixedType"],
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Subtipo do título",
            displayValue = draft.fixedSubType?.asLabel().orEmpty(),
            options = FixedIncomeSubType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fixedSubType = it)) },
            error = fieldErrors["fixedSubType"],
            required = true,
        )
    }

    item {
        StableExposedDropdown(
            label = "Liquidez do título",
            displayValue = draft.fixedLiquidity?.asLabel().orEmpty(),
            options = Liquidity.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fixedLiquidity = it)) },
            error = fieldErrors["fixedLiquidity"],
            required = true,
        )
    }

    item {
        TextField(
            value = draft.fixedExpiration.orEmpty(),
            onValueChange = { raw -> onDraftChange(draft.copy(fixedExpiration = filterDateMaskDigits(raw))) },
            label = { Text("Data de vencimento") },
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            isError = fieldErrors["fixedExpiration"] != null,
            supportingText = fieldErrors["fixedExpiration"]?.let { { Text(it) } } ?: { Text("Obrigatório") },
        )
    }

    item {
        TextField(
            value = draft.fixedYield.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fixedYield = it)) },
            label = { Text("Rentabilidade (% ao ano)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = fieldErrors["fixedYield"] != null,
            supportingText = fieldErrors["fixedYield"]?.let { { Text(it) } } ?: { Text("Obrigatório") },
        )
    }

    item {
        TextField(
            value = draft.fixedCdi.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fixedCdi = it)) },
            label = { Text("% em relação ao CDI") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = fieldErrors["fixedCdi"] != null,
            supportingText = fieldErrors["fixedCdi"]?.let { { Text(it) } },
        )
    }
}

private fun LazyGridScope.variableIncomeFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {

    item {
        StableExposedDropdown(
            label = "Tipo (RV)",
            displayValue = draft.variableType?.asLabel().orEmpty(),
            options = VariableIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(variableType = it)) },
            error = fieldErrors["variableType"],
        )
    }

    item {
        TextField(
            value = draft.variableTicker.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(variableTicker = it)) },
            label = { Text("Ticker (código na bolsa)") },
            isError = fieldErrors["variableTicker"] != null,
            supportingText = fieldErrors["variableTicker"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        TextField(
            value = draft.variableCnpj.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(variableCnpj = it)) },
            label = { Text("CNPJ do emissor (opcional)") },
            isError = fieldErrors["cnpj"] != null,
            supportingText = fieldErrors["cnpj"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun LazyGridScope.fundFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {

    item {
        TextField(
            value = draft.fundName.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fundName = it)) },
            label = { Text("Identificação") },
            isError = fieldErrors["fundName"] != null,
            supportingText = fieldErrors["fundName"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        StableExposedDropdown(
            label = "Categoria do fundo",
            displayValue = draft.fundType?.asLabel().orEmpty(),
            options = InvestmentFundAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fundType = it)) },
            error = fieldErrors["fundType"],
        )
    }

    item {
        TextField(
            value = draft.fundLiquidityDays.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fundLiquidityDays = it)) },
            label = { Text("Resgate em (dias úteis)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = fieldErrors["fundLiquidityDays"] != null,
            supportingText = fieldErrors["fundLiquidityDays"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    item {
        TextField(
            value = draft.fundExpiration.orEmpty(),
            onValueChange = { raw -> onDraftChange(draft.copy(fundExpiration = filterDateMaskDigits(raw))) },
            label = { Text("Data de vencimento") },
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            isError = fieldErrors["fundExpiration"] != null,
            supportingText = fieldErrors["fundExpiration"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
