package com.eferraz.presentation.features.assetForm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.presentation.design_system.components.EnumDropdown
import com.eferraz.presentation.design_system.components.FormTextField
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.AssetFormData
import com.eferraz.usecases.FixedIncomeFormData
import com.eferraz.usecases.InvestmentFundFormData
import com.eferraz.usecases.VariableIncomeFormData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetFormScreen(
    state: AssetFormViewModel.AssetFormState,
    onIntent: (AssetFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar Ativo" else "Novo Ativo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {

        AssetFormScreenStructure(
            modifier = Modifier.padding(it),
            formData = state.formData,
            header = {
                item {
                    EnumDropdown(
                        label = "Categoria",
                        value = state.formData.category,
                        options = InvestmentCategory.entries,
                        optionLabel = { it.formated() },
                        onValueChange = { onIntent(AssetFormIntent.UpdateCategory(it)) },
                        enabled = !state.isEditMode,
                        errorMessage = state.validationErrors["category"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            fixedIncome = { formData ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        EnumDropdown(
                            label = "Tipo",
                            value = formData.type,
                            options = FixedIncomeAssetType.entries,
                            optionLabel = { it?.formated() ?: "" },
                            onValueChange = { onIntent(AssetFormIntent.UpdateType(it)) },
                            errorMessage = state.validationErrors["type"],
                            modifier = Modifier.weight(1f),
                        )

                        EnumDropdown(
                            label = "Subtipo",
                            value = formData.subType,
                            options = FixedIncomeSubType.entries,
                            optionLabel = { it?.formated() ?: "" },
                            onValueChange = { onIntent(AssetFormIntent.UpdateSubType(it)) },
                            errorMessage = state.validationErrors["subType"],
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        EnumDropdown(
                            label = "Liquidez",
                            value = formData.liquidity,
                            options = listOf(Liquidity.DAILY, Liquidity.AT_MATURITY).map { it as Liquidity? },
                            optionLabel = { it?.formated() ?: "" },
                            onValueChange = { onIntent(AssetFormIntent.UpdateLiquidity(it)) },
                            errorMessage = state.validationErrors["liquidity"],
                            modifier = Modifier.weight(1f),
                        )

                        FormTextField(
                            label = "Vencimento",
                            value = formData.expirationDate ?: "",
                            onValueChange = { onIntent(AssetFormIntent.UpdateExpirationDate(it)) },
                            placeholder = "YYYY-MM-DD",
                            validationErrors = state.validationErrors,
                            errorKey = "expirationDate",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FormTextField(
                            label = "Rentabilidade",
                            value = formData.contractedYield.orEmpty(),
                            onValueChange = { onIntent(AssetFormIntent.UpdateContractedYield(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            validationErrors = state.validationErrors,
                            errorKey = "contractedYield",
                            modifier = Modifier.weight(1f)
                        )

                        FormTextField(
                            label = "Relativa ao CDI",
                            value = formData.cdiRelativeYield.orEmpty(),
                            onValueChange = { onIntent(AssetFormIntent.UpdateCdiRelativeYield(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            validationErrors = state.validationErrors,
                            errorKey = "cdiRelativeYield",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            investmentFund = { formData ->
                item {
                    EnumDropdown(
                        label = "Tipo de Fundo",
                        value = formData.type,
                        options = InvestmentFundAssetType.entries,
                        optionLabel = { it?.formated() ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateFundType(it)) },
                        errorMessage = state.validationErrors["type"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    FormTextField(
                        label = "Nome",
                        value = formData.name.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateFundName(it)) },
                        validationErrors = state.validationErrors,
                        errorKey = "name",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        FormTextField(
                            label = "Dias para Resgate",
                            value = formData.liquidityDays.orEmpty(),
                            onValueChange = { onIntent(AssetFormIntent.UpdateLiquidityDays(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            validationErrors = state.validationErrors,
                            errorKey = "liquidityDays",
                            modifier = Modifier.weight(1f)
                        )

                        FormTextField(
                            label = "Vencimento",
                            value = formData.expirationDate ?: "",
                            onValueChange = { onIntent(AssetFormIntent.UpdateFundExpirationDate(it)) },
                            placeholder = "YYYY-MM-DD",
                            validationErrors = state.validationErrors,
                            errorKey = "expirationDate",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            variableIncome = { formData ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        EnumDropdown(
                            label = "Tipo",
                            value = formData.type,
                            options = VariableIncomeAssetType.entries,
                            optionLabel = { it?.formated() ?: "" },
                            onValueChange = { onIntent(AssetFormIntent.UpdateVariableType(it)) },
                            errorMessage = state.validationErrors["type"],
                            modifier = Modifier.weight(1f),
                        )

                        FormTextField(
                            label = "Ticker",
                            value = formData.ticker.orEmpty(),
                            onValueChange = { onIntent(AssetFormIntent.UpdateTicker(it)) },
                            validationErrors = state.validationErrors,
                            errorKey = "ticker",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            common = {
                item {
                    EnumDropdown(
                        label = "Emissor",
                        value = state.formData.issuerName,
                        options = state.issuers,
                        optionLabel = { it ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateIssuerName(it)) },
                        errorMessage = state.validationErrors["issuer"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    FormTextField(
                        label = "Observações",
                        value = state.formData.observations.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateObservations(it)) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            actions = {
                Button(onClick = { onIntent(AssetFormIntent.Save) }) {
                    Text("Salvar")
                }
            }
        )
    }
}

@Composable
private fun AssetFormScreenStructure(
    modifier: Modifier = Modifier,
    formData: AssetFormData,
    header: LazyListScope.() -> Unit,
    fixedIncome: LazyListScope.(FixedIncomeFormData) -> Unit,
    investmentFund: LazyListScope.(InvestmentFundFormData) -> Unit,
    variableIncome: LazyListScope.(VariableIncomeFormData) -> Unit,
    common: LazyListScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {

    Column(modifier) {

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            header()

            when (formData) {
                is FixedIncomeFormData -> fixedIncome(formData)
                is InvestmentFundFormData -> investmentFund(formData)
                is VariableIncomeFormData -> variableIncome(formData)
            }

            common()
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            content = actions
        )
    }
}