package com.eferraz.asset_management.assets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.FormTextField
import com.eferraz.asset_management.transactions.TransactionFormView
import com.eferraz.design_system.components.dropdown.AppDropdownField
import com.eferraz.design_system.components.segmented_control.SegmentedControl
import com.eferraz.design_system.components.segmented_control.SegmentedControlChoice
import com.eferraz.design_system.core.StableList
import com.eferraz.design_system.input.date.DateFormat
import com.eferraz.design_system.input.date.DateVisualTransformation
import com.eferraz.design_system.scaffolds.AppContentDialog
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
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
        modifier = modifier.width(1000.dp).padding(vertical = 36.dp),
        title = if (holdingId != null) "Editar investimento" else "Novo investimento",
        leadingIcon = Icons.Outlined.Info,
        onDismiss = onDismiss
    ) {

        AssetManagementScreen(
            modifier = Modifier,
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
        modifier = modifier.verticalScroll(rememberScrollState()),
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
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        FormSection(
            title = "ATIVO",
            icon = Icons.Outlined.AttachMoney
        ) {

            when (ui.assetClass) {
                AssetClass.FIXED_INCOME -> FixedIncomeFields(ui, onEvent)
                AssetClass.VARIABLE_INCOME -> VariableIncomeFields(ui, onEvent)
                AssetClass.INVESTMENT_FUND -> FundFields(ui, onEvent)
            }

            FormCardActions(
                isVisible = true,
                onClick = { onEvent(AssetManagementEvents.Save) },
                enabled = !ui.isSaving
            )
        }

        var isVisible by remember { mutableStateOf(true) }


        FormSection(
            title = "POSICIONAMENTO",
            icon = Icons.Outlined.Home
        ) {

            FormRow {

                AppDropdownField(
                    modifier = Modifier.width(250.dp),
                    label = "Titular",
                    displayValue = ui.brokerage?.name.orEmpty(),
                    options = ui.brokerages,
                    itemLabel = { it.name },
                    onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
                    error = ui.brokerageError,
                    required = true,
                )

                AppDropdownField(
                    modifier = Modifier.weight(1f),
                    label = BROKERAGE_FIELD_LABEL,
                    displayValue = ui.brokerage?.name.orEmpty(),
                    options = ui.brokerages,
                    itemLabel = { it.name },
                    onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
                    error = ui.brokerageError,
                    required = true,
                )
            }


            FormCardActions(
                isVisible = isVisible,
                onClick = {}
            )
        }

        FormSection(
            title = "TRANSAÇÕES",
            icon = Icons.Outlined.SwapVert
        ) {

            FormRow {

                TransactionFormView(
                    holdingId = 1,
                    onComplete = {},
                )
            }

            FormCardActions(
                onClick = {
                    isVisible = !isVisible
                }
            )
        }

        FormSection(
            title = "RESUMO",
            icon = Icons.Outlined.Summarize
        ) {

            FormRow {

                FormTextField(
                    modifier = Modifier.weight(1f),
                    label = "Balanço das Transações",
                    value = "1.000,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )

                FormTextField(
                    modifier = Modifier.weight(1f),
                    label = "Valor Atual (Bruto)",
                    value = "1.000,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )

                FormTextField(
                    modifier = Modifier.weight(1f),
                    label = "Lucro Bruto",
                    value = "1.000,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )
            }

            FormRow {

                FormTextField(
                    modifier = Modifier.weight(1f),
                    label = "Impostos",
                    value = "100,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )

                FormTextField(
                    modifier = Modifier.weight(1f),
                    label = "Total Líquido",
                    value = "1.000,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )

                FormTextField(
                    modifier = Modifier.weight(.6f),
                    label = "Lucro Líquido",
                    value = "1.000,00",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    prefix = { Text("R$ ") }
                )

                FormTextField(
                    modifier = Modifier.weight(.35f),
                    label = " ",
                    value = "22,5",
                    onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
                    errorMessage = ui.fixedYieldError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true,
                    suffix = { Text("%") }
                )
            }
        }
    }

    Surface {

        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Button(
                onClick = { /* TODO NOT IMPLEMENTED YET */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Excluir")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            FilledTonalButton(
                onClick = {/* TODO NOT IMPLEMENTED YET */ }
            ) {
                Text("Concluir")
            }
        }
    }
}

@Composable
private fun FormSection(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {

    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
        ) {

            FormHeader(
                title = title,
                icon = icon
            )

            Column(
                modifier = Modifier.padding(top = 24.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun FormRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {

    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun FormHeader(
    title: String,
    icon: ImageVector,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = DividerDefaults.color.copy(alpha = 0.4f))
}


@Composable
private fun FixedIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Classe do Ativo",
            displayValue = ui.assetClass.asLabel(),
            options = AssetClass.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
            enabled = ui.asset == null,
            required = true,
        )

        AppDropdownField(
            modifier = Modifier.weight(0.9f),
            label = "Tipo",
            displayValue = ui.fixedType?.asLabel().orEmpty(),
            options = FixedIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.FixedTypeChanged(it)) },
            error = ui.fixedTypeError,
            required = true,
        )

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = ui.issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )
    }

    FormRow {

        AppDropdownField(
            modifier = Modifier.weight(.35f),
            label = "Liquidez",
            displayValue = ui.fixedLiquidity?.asLabel().orEmpty(),
            options = Liquidity.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.FixedLiquidityChanged(it)) },
            error = ui.fixedLiquidityError,
            required = true,
        )

        FormTextField(
            modifier = Modifier.weight(.35f),
            label = "Vencimento",
            value = ui.fixedExpiration.orEmpty(),
            onValueChange = { raw -> onEvent(AssetManagementEvents.FixedExpirationChanged(raw)) },
            errorMessage = ui.fixedExpirationError,
            placeholder = { Text("AAAA-MM-DD") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = remember { DateVisualTransformation(DateFormat.YYYY_MM_DD) },
            trailingIcon = {
                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null)
            }
        )

        AppDropdownField(
            modifier = Modifier.weight(.45f),
            label = "Indexador",
            displayValue = ui.yieldIndexer?.asLabel().orEmpty(),
            options = YieldIndexer.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.YieldIndexerChanged(it)) },
            error = ui.yieldIndexerError,
            required = true,
        )

        FormTextField(
            modifier = Modifier.weight(.2f),
            label = "Rent. (a.a.)",
            value = ui.fixedYield.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FixedYieldChanged(it)) },
            errorMessage = ui.fixedYieldError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("%") }
        )

        FormTextField(
            modifier = Modifier.weight(.2f),
            label = "Rent. (CDI)",
            value = ui.fixedCdi.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FixedCdiChanged(it)) },
            errorMessage = ui.fixedCdiError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("%") }
        )
    }

    FormRow {

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )

        FormTextField(
            modifier = Modifier.weight(.4f),
            label = B3_IDENTIFIER_FIELD_LABEL,
            value = ui.b3Identifier.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.B3IdentifierChanged(it)) },
            errorMessage = null,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            }
        )

        Column(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            "Isendo de IR".takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            var selected by remember { mutableStateOf(SegmentedControlChoice("Não", "Não")) }

            SegmentedControl(
                modifier = Modifier.height(55.dp),
                selected = selected,
                options = StableList(listOf(SegmentedControlChoice("Sim", "Sim"), SegmentedControlChoice("Não", "Não"))),
                onSelect = { choice ->
                    selected = choice
                },
            )
        }
    }
}

@Composable
private fun VariableIncomeFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Classe do Ativo",
            displayValue = ui.assetClass.asLabel(),
            options = AssetClass.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
            enabled = ui.asset == null,
            required = true,
        )

        AppDropdownField(
            modifier = Modifier.weight(0.9f),
            label = "Tipo",
            displayValue = ui.variableType?.asLabel().orEmpty(),
            options = VariableIncomeAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.VariableTypeChanged(it)) },
            error = ui.variableTypeError,
        )

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = ui.issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )
    }

    FormRow {

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )

        FormTextField(
            modifier = Modifier.weight(.4f),
            label = B3_IDENTIFIER_FIELD_LABEL,
            value = ui.b3Identifier.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.B3IdentifierChanged(it)) },
            errorMessage = null,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            }
        )
    }
}

@Composable
private fun FundFields(
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    FormRow {

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Classe do Ativo",
            displayValue = ui.assetClass.asLabel(),
            options = AssetClass.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.AssetClassChanged(it)) },
            enabled = ui.asset == null,
            required = true,
        )

        AppDropdownField(
            modifier = Modifier.weight(0.9f),
            label = "Tipo",
            displayValue = ui.fundType?.asLabel().orEmpty(),
            options = InvestmentFundAssetType.entries.toList(),
            itemLabel = { it.asLabel() },
            onItemSelect = { onEvent(AssetManagementEvents.FundTypeChanged(it)) },
            error = ui.fundTypeError,
        )

        AppDropdownField(
            modifier = Modifier.weight(1f),
            label = "Emissor",
            displayValue = ui.issuer?.name.orEmpty(),
            options = ui.issuers,
            itemLabel = { it.name },
            onItemSelect = { issuer -> onEvent(AssetManagementEvents.IssuerChanged(issuer)) },
            error = ui.issuerError,
            required = true,
        )
    }

    FormRow {

        FormTextField(
            label = "Identificação",
            value = ui.fundName.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.FundNameChanged(it)) },
            errorMessage = ui.fundNameError,
            modifier = Modifier.weight(1f),
        )

        FormTextField(
            modifier = Modifier.weight(1f),
            label = "Observações gerais",
            value = ui.observations.orEmpty(),
            onValueChange = { onEvent(AssetManagementEvents.ObservationsChanged(it)) },
            errorMessage = null,
        )
    }
}

@Composable
private fun FormCardActions(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {

        Column(
            modifier = modifier.padding(top = 16.dp)
        ) {

            HorizontalDivider(modifier = Modifier.padding(top = 0.dp, bottom = 16.dp), color = DividerDefaults.color.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.Bottom,
            ) {

                Button(
                    onClick = onClick,
                    enabled = enabled
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

private class AssetFormPreviewProvider : PreviewParameterProvider<AssetManagementUiState> {

    override val values: Sequence<AssetManagementUiState> = sequenceOf(
        AssetManagementUiState(assetClass = AssetClass.FIXED_INCOME, brokerages = listOf(Brokerage(1L, "XP Investimentos"))),
        AssetManagementUiState(assetClass = AssetClass.VARIABLE_INCOME),
        AssetManagementUiState(assetClass = AssetClass.INVESTMENT_FUND),
    )

    override fun getDisplayName(index: Int) = when (index) {
        0 -> "Fixed Income"
        1 -> "Variable Income"
        2 -> "Investment Fund"
        else -> null
    }
}

@Preview(widthDp = 1000, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(widthDp = 1000, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AssetManagementScreenPreview(
    @PreviewParameter(AssetFormPreviewProvider::class) ui: AssetManagementUiState,
) {

    AppThemeV2 {

        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {

            AppContentDialog(
                modifier = Modifier.width(800.dp).padding(16.dp),
                title = "Novo investimento",
                onDismiss = {}
            ) {

                AssetFormView(
                    modifier = Modifier,
                    ui = ui,
                    onEvent = {}
                )
            }
        }
    }
}
