package com.eferraz.presentation.features.assetForm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity
import com.eferraz.presentation.design_system.components.EnumDropdown
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.FixedIncomeFormData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetFormContent(
    state: AssetFormViewModel.AssetFormState,
    onIntent: (AssetFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Novo Ativo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {

        Column(Modifier.padding(it)) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
                    .padding(16.dp),
            ) {

                EnumDropdown(
                    label = "Categoria",
                    value = state.formData.category,
                    options = InvestmentCategory.entries,
                    optionLabel = { it.formated() },
                    onValueChange = { onIntent(AssetFormIntent.UpdateCategory(it)) },
                    errorMessage = state.validationErrors["category"],
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                when (state.formData) {
                    is FixedIncomeFormData -> FixedIncomeForm(state, onIntent)
                }

                Spacer(modifier = Modifier.height(16.dp))

                EnumDropdown(
                    label = "Emissor",
                    value = state.formData.issuerName,
                    options = state.issuers,
                    optionLabel = { it ?: "" },
                    onValueChange = { onIntent(AssetFormIntent.UpdateIssuerName(it)) },
                    errorMessage = state.validationErrors["issuer"],
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Observações
                OutlinedTextField(
                    label = { Text("Observações", style = MaterialTheme.typography.bodyMedium) },
                    value = state.formData.observations.orEmpty(),
                    onValueChange = { onIntent(AssetFormIntent.UpdateObservations(it)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botões de ação
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End,
            ) {

                Button(onClick = { onIntent(AssetFormIntent.Save) }) {
                    Text("Salvar")
                }
            }
        }
    }
}

@Composable
private fun FixedIncomeForm(
    state: AssetFormViewModel.AssetFormState,
    onIntent: (AssetFormIntent) -> Unit,
) {

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

        EnumDropdown(
            label = "Tipo",
            value = state.formData.type,
            options = FixedIncomeAssetType.entries,
            optionLabel = { it?.formated() ?: "" },
            onValueChange = { onIntent(AssetFormIntent.UpdateType(it)) },
            errorMessage = state.validationErrors["type"],
            modifier = Modifier.weight(1f),
        )

        EnumDropdown(
            label = "Subtipo",
            value = state.formData.subType,
            options = FixedIncomeSubType.entries,
            optionLabel = { it?.formated() ?: "" },
            onValueChange = { onIntent(AssetFormIntent.UpdateSubType(it)) },
            errorMessage = state.validationErrors["subType"],
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

        EnumDropdown(
            label = "Liquidez",
            value = state.formData.liquidity,
            options = listOf(Liquidity.DAILY, Liquidity.AT_MATURITY).map { it as Liquidity? },
            optionLabel = { it?.formated() ?: "" },
            onValueChange = { onIntent(AssetFormIntent.UpdateLiquidity(it)) },
            errorMessage = state.validationErrors["liquidity"],
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            label = { Text("Vencimento", style = MaterialTheme.typography.bodyMedium) },
            value = state.formData.expirationDate ?: "",
            onValueChange = { onIntent(AssetFormIntent.UpdateExpirationDate(it)) },
            placeholder = { Text("YYYY-MM-DD", style = MaterialTheme.typography.bodyMedium) },
            isError = state.validationErrors.containsKey("expirationDate"),
            supportingText = state.validationErrors["expirationDate"]?.let { { Text(it) } },
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

        OutlinedTextField(
            label = { Text("Rentabilidade", style = MaterialTheme.typography.bodyMedium) },
            value = state.formData.contractedYield.orEmpty(),
            onValueChange = { onIntent(AssetFormIntent.UpdateContractedYield(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.validationErrors.containsKey("contractedYield"),
            supportingText = state.validationErrors["contractedYield"]?.let { { Text(it) } },
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            label = { Text("Relativa ao CDI", style = MaterialTheme.typography.bodyMedium) },
            value = state.formData.cdiRelativeYield.orEmpty(),
            onValueChange = { onIntent(AssetFormIntent.UpdateCdiRelativeYield(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.validationErrors.containsKey("cdiRelativeYield"),
            supportingText = state.validationErrors["cdiRelativeYield"]?.let { { Text(it) } },
            modifier = Modifier.weight(1f),
        )
    }
}