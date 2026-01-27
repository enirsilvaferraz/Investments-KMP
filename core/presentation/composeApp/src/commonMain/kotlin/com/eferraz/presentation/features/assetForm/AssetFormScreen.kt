package com.eferraz.presentation.features.assetForm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.presentation.design_system.components.EnumDropdown
import com.eferraz.presentation.design_system.components.FormTextField
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.entities.AssetFormData
import com.eferraz.usecases.entities.FixedIncomeFormData
import com.eferraz.usecases.entities.InvestmentFundFormData
import com.eferraz.usecases.entities.VariableIncomeFormData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetFormScreen(
    state: AssetFormState,
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
                item(span = { GridItemSpan(2) }) {
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
                    EnumDropdown(
                        label = "Tipo",
                        value = formData.type,
                        options = FixedIncomeAssetType.entries,
                        optionLabel = { it?.formated() ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateType(it)) },
                        errorMessage = state.validationErrors["type"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    EnumDropdown(
                        label = "Subtipo",
                        value = formData.subType,
                        options = FixedIncomeSubType.entries,
                        optionLabel = { it?.formated() ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateSubType(it)) },
                        errorMessage = state.validationErrors["subType"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    EnumDropdown(
                        label = "Liquidez",
                        value = formData.liquidity,
                        options = listOf(Liquidity.DAILY, Liquidity.AT_MATURITY).map { it as Liquidity? },
                        optionLabel = { it?.formated() ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateLiquidity(it)) },
                        errorMessage = state.validationErrors["liquidity"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    FormTextField(
                        label = "Vencimento",
                        value = formData.expirationDate ?: "",
                        onValueChange = { onIntent(AssetFormIntent.UpdateExpirationDate(it)) },
                        placeholder = "YYYY-MM-DD",
                        validationErrors = state.validationErrors,
                        errorKey = "expirationDate",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    FormTextField(
                        label = "Rentabilidade",
                        value = formData.contractedYield.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateContractedYield(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        validationErrors = state.validationErrors,
                        errorKey = "contractedYield",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    FormTextField(
                        label = "Relativa ao CDI",
                        value = formData.cdiRelativeYield.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateCdiRelativeYield(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        validationErrors = state.validationErrors,
                        errorKey = "cdiRelativeYield",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            investmentFund = { formData ->
                item(span = { GridItemSpan(2) }) {
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

                item(span = { GridItemSpan(2) }) {
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
                    FormTextField(
                        label = "Dias para Resgate",
                        value = formData.liquidityDays.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateLiquidityDays(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        validationErrors = state.validationErrors,
                        errorKey = "liquidityDays",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    FormTextField(
                        label = "Vencimento",
                        value = formData.expirationDate ?: "",
                        onValueChange = { onIntent(AssetFormIntent.UpdateFundExpirationDate(it)) },
                        placeholder = "YYYY-MM-DD",
                        validationErrors = state.validationErrors,
                        errorKey = "expirationDate",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            variableIncome = { formData ->
                item {
                    EnumDropdown(
                        label = "Tipo",
                        value = formData.type,
                        options = VariableIncomeAssetType.entries,
                        optionLabel = { it?.formated() ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateVariableType(it)) },
                        errorMessage = state.validationErrors["type"],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    FormTextField(
                        label = "Ticker",
                        value = formData.ticker.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateTicker(it)) },
                        validationErrors = state.validationErrors,
                        errorKey = "ticker",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            common = {
                item(span = { GridItemSpan(2) }) {
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
                item(span = { GridItemSpan(2) }) {
                    FormTextField(
                        label = "Observações",
                        value = state.formData.observations.orEmpty(),
                        onValueChange = { onIntent(AssetFormIntent.UpdateObservations(it)) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item(span = { GridItemSpan(2) }) {
                    EnumDropdown(
                        label = "Corretora",
                        value = state.formData.brokerageName,
                        options = listOf(null) + state.brokerages,
                        optionLabel = { it ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateBrokerageName(it)) },
                        onNullSelected = { onIntent(AssetFormIntent.UpdateBrokerageName("")) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item(span = { GridItemSpan(2) }) {
                    EnumDropdown(
                        label = "Objetivo",
                        value = state.formData.goalName,
                        options = listOf(null) + state.goals,
                        optionLabel = { it ?: "" },
                        onValueChange = { onIntent(AssetFormIntent.UpdateGoalName(it)) },
                        onNullSelected = { onIntent(AssetFormIntent.UpdateGoalName("")) },
                        enabled = !state.formData.brokerageName.isNullOrBlank(),
                        errorMessage = state.validationErrors["goal"],
                        modifier = Modifier.fillMaxWidth(),
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
    header: LazyGridScope.() -> Unit,
    fixedIncome: LazyGridScope.(FixedIncomeFormData) -> Unit,
    investmentFund: LazyGridScope.(InvestmentFundFormData) -> Unit,
    variableIncome: LazyGridScope.(VariableIncomeFormData) -> Unit,
    common: LazyGridScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {

    Column(modifier) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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