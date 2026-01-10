package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.value.CNPJ
import com.eferraz.entities.value.MaturityDate
import com.eferraz.presentation.FixedIncomeAssetRouting
import com.eferraz.presentation.FundsAssetRouting
import com.eferraz.presentation.VariableIncomeAssetRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.design_system.components.inputs.TableInputDate
import com.eferraz.presentation.design_system.components.inputs.TableInputSelect
import com.eferraz.presentation.design_system.components.inputs.TableInputText
import com.eferraz.presentation.design_system.components.new_table.UiTable
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateBrokerage
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsState
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.usecases.entities.FixedIncomeAssetsTableData
import com.eferraz.usecases.entities.InvestmentFundAssetsTableData
import com.eferraz.usecases.entities.VariableIncomeAssetsTableData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {
    val formVm = koinViewModel<AssetFormViewModel>()
    val formState by formVm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Ativos",
        navigator = navigator,
        mainPane = {
            val backStack = rememberNavBackStack(config, FixedIncomeAssetRouting)

            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<FixedIncomeAssetRouting> {
                            val category = InvestmentCategory.FIXED_INCOME
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenFixedIncome(
                                state = tableState,
                                category = category,
                                viewModel = tableVm
                            )
                        }

                        entry<VariableIncomeAssetRouting> {
                            val category = InvestmentCategory.VARIABLE_INCOME
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenVariableIncome(
                                state = tableState,
                                category = category,
                                viewModel = tableVm
                            )
                        }

                        entry<FundsAssetRouting> {
                            val category = InvestmentCategory.INVESTMENT_FUND
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenFunds(
                                state = tableState,
                                category = category,
                                viewModel = tableVm
                            )
                        }
                    }
                )

                SegmentedControl(
                    options = listOf(
                        SegmentedOption(
                            value = FixedIncomeAssetRouting,
                            label = "Renda Fixa",
                            icon = Icons.Default.Savings,
                            contentDescription = "Renda Fixa"
                        ),
                        SegmentedOption(
                            value = VariableIncomeAssetRouting,
                            label = "Renda Variável",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Renda Variável"
                        ),
                        SegmentedOption(
                            value = FundsAssetRouting,
                            label = "Fundos",
                            icon = Icons.Default.AccountBalance,
                            contentDescription = "Fundos"
                        )
                    ),
                    selectedValue = backStack.lastOrNull() ?: FixedIncomeAssetRouting,
                    onValueChange = { backStack[0] = it },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp).align(Alignment.BottomStart)
                )
            }
        },
        actions = {
            AssetsActions(scope, navigator, formVm)
        },
        extraPane = {
            if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                AssetFormScreen(
                    state = formState,
                    onIntent = { formVm.processIntent(it) },
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun AssetsActions(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Nothing>,
    formVm: AssetFormViewModel,
) {
    FilledIconButton(
        onClick = {
            scope.launch {
                if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                    navigator.navigateBack()
                } else {
                    // Limpar formulário para modo de cadastro
                    formVm.processIntent(AssetFormIntent.ClearForm)
                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                }
            }
        },
        colors = if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
            IconButtonDefaults.filledTonalIconButtonColors()
        else
            IconButtonDefaults.filledIconButtonColors()
    ) {
        if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        else
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun AssetsScreenFixedIncome(
    modifier: Modifier = Modifier,
    state: AssetsState,
    category: InvestmentCategory,
    viewModel: AssetsViewModel,
) {
    val fixedIncomeData = state.tableData.filterIsInstance<FixedIncomeAssetsTableData>()

    UiTable(
        modifier = modifier,
        data = fixedIncomeData,
    ) {

        column(
            header = "Corretora",
            sortedBy = { it.brokerageName },
            cellContent = { row ->
                TableInputSelect(
                    value = state.brokerages.find { it.id == row.brokerageId },
                    options = listOf(null) + state.brokerages,
                    format = { it?.name.orEmpty() },
                    onChange = { value ->
                        viewModel.onIntent(UpdateBrokerage(row.assetId, value, category))
                    }
                )
            }
        )

        column(
            header = "SubCategoria",
            sortedBy = { it.subType },
            cellContent = { row ->
                TableInputSelect(
                    value = row.subType,
                    options = FixedIncomeSubType.entries,
                    format = { it.formated() },
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(subType = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            cellContent = { row ->
                TableInputSelect(
                    value = row.type,
                    options = FixedIncomeAssetType.entries,
                    format = { it.formated() },
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(type = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Vencimento",
            sortedBy = { it.expirationDate },
            cellContent = { row ->
                TableInputDate(
                    value = row.expirationDate.formated(),
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(expirationDate = MaturityDate(value).get())
                        }
                    }
                )
            }
        )

        column(
            header = "Taxa",
            sortedBy = { it.contractedYield },
            cellContent = { row ->
                TableInputText(
                    value = row.contractedYield.toString(),
                    onChange = { value ->
                        value.toDoubleOrNull()?.let { newValue ->
                            viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                                asset.copy(contractedYield = newValue)
                            }
                        }
                    }
                )
            }
        )

        column(
            header = "% CDI",
            sortedBy = { it.cdiRelativeYield ?: 0.0 },
            cellContent = { row ->
                TableInputText(
                    value = row.cdiRelativeYield?.toString().orEmpty(),
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(cdiRelativeYield = value.toDoubleOrNull())
                        }
                    }
                )
            }
        )

        column(
            header = "Emissor",
            sortedBy = { it.issuerName },
            cellContent = { row ->
                state.issuers.find { it.id == row.issuerId }?.let { currentIssuer ->
                    TableInputSelect(
                        value = currentIssuer,
                        options = state.issuers,
                        format = { it.name },
                        onChange = { value ->
                            viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                                asset.copy(issuer = value)
                            }
                        }
                    )
                }
            }
        )

        column(
            header = "Liquidez",
            sortedBy = { it.liquidity },
            cellContent = { row ->
                TableInputSelect(
                    value = row.liquidity,
                    options = Liquidity.entries,
                    format = { it.formated() },
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(liquidity = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            cellContent = { row ->
                TableInputText(
                    value = row.observations,
                    onChange = { value ->
                        viewModel.updateFixedIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(observations = value.ifEmpty { null })
                        }
                    }
                )
            }
        )
    }
}


@Composable
private fun AssetsScreenVariableIncome(
    modifier: Modifier = Modifier,
    state: AssetsState,
    category: InvestmentCategory,
    viewModel: AssetsViewModel,
) {
    val variableIncomeData = state.tableData.filterIsInstance<VariableIncomeAssetsTableData>()

    UiTable(
        modifier = modifier,
        data = variableIncomeData,
        onSelect = { row -> /* onRowClick(row.assetId) */ }
    ) {

        column(
            header = "Corretora",
            sortedBy = { it.brokerageName },
            cellContent = { row ->
                TableInputSelect(
                    value = state.brokerages.find { it.id == row.brokerageId },
                    options = listOf(null) + state.brokerages,
                    format = { it?.name.orEmpty() },
                    onChange = { value ->
                        viewModel.onIntent(UpdateBrokerage(row.assetId, value, category))
                    }
                )
            }
        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            cellContent = { row ->
                TableInputSelect(
                    value = row.type,
                    options = VariableIncomeAssetType.entries,
                    format = { it.formated() },
                    onChange = { value ->
                        viewModel.updateVariableIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(type = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Ticker",
            sortedBy = { it.ticker },
            cellContent = { row ->
                TableInputText(
                    value = row.ticker,
                    onChange = { value ->
                        viewModel.updateVariableIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(ticker = value)
                        }
                    }
                )
            }
        )

        column(
            header = "CNPJ",
            sortedBy = { it.cnpj },
            cellContent = { row ->
                TableInputText(
                    value = row.cnpj,
                    onChange = { value ->
                        val cnpj = if (value.isBlank()) {
                            null
                        } else {
                            try {
                                CNPJ(value)
                            } catch (e: IllegalArgumentException) {
                                return@TableInputText
                            }
                        }
                        viewModel.updateVariableIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(cnpj = cnpj)
                        }
                    }
                )
            }
        )

        column(
            header = "Nome",
            sortedBy = { it.name },
            cellContent = { row ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = row.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            cellContent = { row ->
                TableInputText(
                    value = row.observations,
                    onChange = { value ->
                        viewModel.updateVariableIncomeAsset(row.assetId, category) { asset ->
                            asset.copy(observations = value.ifEmpty { null })
                        }
                    }
                )
            }
        )
    }
}


@Composable
private fun AssetsScreenFunds(
    modifier: Modifier = Modifier,
    state: AssetsState,
    category: InvestmentCategory,
    viewModel: AssetsViewModel,
) {
    val fundsData = state.tableData.filterIsInstance<InvestmentFundAssetsTableData>()

    UiTable(
        modifier = modifier,
        data = fundsData,
        onSelect = { row -> /* onRowClick(row.assetId) */ }
    ) {
        column(
            header = "Corretora",
            sortedBy = { it.brokerageName },
            cellContent = { row ->
                TableInputSelect(
                    value = state.brokerages.find { it.id == row.brokerageId },
                    options = listOf(null) + state.brokerages,
                    format = { it?.name.orEmpty() },
                    onChange = { value ->
                        viewModel.onIntent(UpdateBrokerage(row.assetId, value, category))
                    }
                )
            }
        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            cellContent = { row ->
                TableInputSelect(
                    value = row.type,
                    options = InvestmentFundAssetType.entries,
                    format = { it.formated() },
                    onChange = { value ->
                        viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                            asset.copy(type = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Nome",
            sortedBy = { it.name },
            cellContent = { row ->
                TableInputText(
                    value = row.name,
                    onChange = { value ->
                        viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                            asset.copy(name = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Liquidez",
            sortedBy = { it.liquidity },
            cellContent = { row ->
                TableInputSelect(
                    value = row.liquidity,
                    options = Liquidity.entries,
                    format = { it.formated(row.liquidityDays) },
                    onChange = { value ->
                        viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                            asset.copy(liquidity = value)
                        }
                    }
                )
            }
        )

        column(
            header = "Dias Liq.",
            sortedBy = { it.liquidityDays },
            cellContent = { row ->
                TableInputText(
                    value = row.liquidityDays.toString(),
                    onChange = { value ->
                        value.toIntOrNull()?.let { newValue ->
                            viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                                asset.copy(liquidityDays = newValue)
                            }
                        }
                    }
                )
            }
        )

        column(
            header = "Vencimento",
            sortedBy = { it.expirationDate ?: LocalDate(1900, 1, 1) },
            cellContent = { row ->
                TableInputDate(
                    value = row.expirationDate?.formated() ?: "",
                    onChange = { value ->
                        viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                            asset.copy(expirationDate = MaturityDate(value).get())
                        }
                    }
                )
            }
        )

        column(
            header = "Emissor",
            sortedBy = { it.issuerName },
            cellContent = { row ->
                state.issuers.find { it.id == row.issuerId }?.let { currentIssuer ->
                    TableInputSelect(
                        value = currentIssuer,
                        options = state.issuers,
                        format = { it.name },
                        onChange = { value ->
                            viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                                asset.copy(issuer = value)
                            }
                        }
                    )
                }
            }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations },
            cellContent = { row ->
                TableInputText(
                    value = row.observations,
                    onChange = { value ->
                        viewModel.updateInvestmentFundAsset(row.assetId, category) { asset ->
                            asset.copy(observations = value.ifEmpty { null })
                        }
                    }
                )
            }
        )
    }
}


