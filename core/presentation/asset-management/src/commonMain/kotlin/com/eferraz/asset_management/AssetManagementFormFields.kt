package com.eferraz.asset_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.asLabel
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.design_system.core.StableList
import com.eferraz.design_system.core.StableMap
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.input.date.filterDateMaskDigits
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType

@Composable
internal fun FixedIncomeFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StableExposedDropdown(
                modifier = Modifier.weight(1f),
                label = "Tipo de cálculo",
                displayValue = draft.fixedType?.asLabel().orEmpty(),
                options = StableList(FixedIncomeAssetType.entries.toList()),
                itemLabel = { it.asLabel() },
                onItemSelect = { onDraftChange(draft.copy(fixedType = it)) },
                error = fieldErrors["fixedType"],
            )

            StableExposedDropdown(
                modifier = Modifier.weight(1f),
                label = "Subtipo do título",
                displayValue = draft.fixedSubType?.asLabel().orEmpty(),
                options = StableList(FixedIncomeSubType.entries.toList()),
                itemLabel = { it.asLabel() },
                onItemSelect = { onDraftChange(draft.copy(fixedSubType = it)) },
                error = fieldErrors["fixedSubType"],
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StableExposedDropdown(
                label = "Liquidez do título",
                displayValue = draft.fixedLiquidity?.let { it.asLabel() }.orEmpty(),
                options = StableList(Liquidity.entries.toList()),
                itemLabel = { it.asLabel() },
                onItemSelect = { onDraftChange(draft.copy(fixedLiquidity = it)) },
                error = fieldErrors["fixedLiquidity"],
                modifier = Modifier.weight(1f),
            )

            OutlinedTextField(
                value = draft.fixedExpiration.orEmpty(),
                onValueChange = { raw -> onDraftChange(draft.copy(fixedExpiration = filterDateMaskDigits(raw))) },
                label = { Text("Data de vencimento") },
                placeholder = { Text("AAAA-MM-DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
                isError = fieldErrors["fixedExpiration"] != null,
                supportingText = fieldErrors["fixedExpiration"]?.let { { Text(it) } },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = draft.fixedYield.orEmpty(),
                onValueChange = { onDraftChange(draft.copy(fixedYield = it)) },
                label = { Text("Rentabilidade (% ao ano)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = fieldErrors["fixedYield"] != null,
                supportingText = fieldErrors["fixedYield"]?.let { { Text(it) } },
            )

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = draft.fixedCdi.orEmpty(),
                onValueChange = { onDraftChange(draft.copy(fixedCdi = it)) },
                label = { Text("% em relação ao CDI") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = fieldErrors["fixedCdi"] != null,
                supportingText = fieldErrors["fixedCdi"]?.let { { Text(it) } },
            )
        }
    }
}

@Composable
internal fun VariableIncomeFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        OutlinedTextField(
            value = draft.variableName.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(variableName = it)) },
            label = { Text("Nome do ativo (como na corretora)") },
            isError = fieldErrors["variableName"] != null,
            supportingText = fieldErrors["variableName"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        StableExposedDropdown(
            label = "Tipo (RV)",
            displayValue = draft.variableType?.asLabel().orEmpty(),
            options = StableList(VariableIncomeAssetType.entries.toList()),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(variableType = it)) },
            error = fieldErrors["variableType"],
        )

        OutlinedTextField(
            value = draft.variableTicker.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(variableTicker = it)) },
            label = { Text("Ticker (código na bolsa)") },
            isError = fieldErrors["variableTicker"] != null,
            supportingText = fieldErrors["variableTicker"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = draft.variableCnpj.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(variableCnpj = it)) },
            label = { Text("CNPJ do emissor (opcional)") },
            isError = fieldErrors["cnpj"] != null,
            supportingText = fieldErrors["cnpj"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Liquidez: D+2 (definida no sistema; não editável)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun FundFields(
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    onDraftChange: (AssetDraft) -> Unit,
) {

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        OutlinedTextField(
            value = draft.fundName.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fundName = it)) },
            label = { Text("Nome do fundo (como no regulamento)") },
            isError = fieldErrors["fundName"] != null,
            supportingText = fieldErrors["fundName"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        StableExposedDropdown(
            label = "Categoria do fundo",
            displayValue = draft.fundType?.asLabel().orEmpty(),
            options = StableList(InvestmentFundAssetType.entries.toList()),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fundType = it)) },
            error = fieldErrors["fundType"],
        )

        StableExposedDropdown(
            label = "Liquidez do fundo",
            displayValue = draft.fundLiquidity?.asLabel().orEmpty(),
            options = StableList(Liquidity.entries.toList()),
            itemLabel = { it.asLabel() },
            onItemSelect = { onDraftChange(draft.copy(fundLiquidity = it)) },
            error = fieldErrors["fundLiquidity"],
        )

        OutlinedTextField(
            value = draft.fundLiquidityDays.orEmpty(),
            onValueChange = { onDraftChange(draft.copy(fundLiquidityDays = it)) },
            label = { Text("Prazo para resgate (dias úteis)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = fieldErrors["fundLiquidityDays"] != null,
            supportingText = fieldErrors["fundLiquidityDays"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = draft.fundExpiration.orEmpty(),
            onValueChange = { raw -> onDraftChange(draft.copy(fundExpiration = filterDateMaskDigits(raw))) },
            label = { Text("Data de encerramento do fundo (opcional)") },
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            isError = fieldErrors["fundExpiration"] != null,
            supportingText = fieldErrors["fundExpiration"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
