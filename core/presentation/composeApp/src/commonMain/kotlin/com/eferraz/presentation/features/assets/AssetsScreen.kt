package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity
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
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.TableColumn
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {


    val formVm = koinViewModel<AssetFormViewModel>()
    val formState by formVm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

//    // Recarrega assets quando o formulário salva e fecha o painel se necessário
//    LaunchedEffect(formState.message, formState.shouldCloseForm) {
//        if (formState.message != null) {
//            tableVm.loadAssets()
//
//            // Fechar o painel se foi salvo com sucesso
//            if (formState.shouldCloseForm && navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
//                navigator.navigateBack()
//                // Resetar o flag após usar
//                formVm.processIntent(AssetFormIntent.ResetCloseFlag)
//            }
//        }
//    }

    AppScaffold(
        title = "Ativos",
        navigator = navigator,
        mainPane = {

            val backStack = rememberNavBackStack(config, FixedIncomeAssetRouting)

            @Composable
            @OptIn(ExperimentalMaterial3AdaptiveApi::class)
            fun screen(category: InvestmentCategory) {

                val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                val tableState by tableVm.state.collectAsStateWithLifecycle()
                tableVm.loadAssets(category)

                AssetsScreen(
                    category = category,
                    state = tableState,
                    onRowClick = { assetId ->
                        scope.launch {
                            formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
                            navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                        }
                    },
                    onIntent = { tableVm.onIntent(it) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {

                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<FixedIncomeAssetRouting> { screen(InvestmentCategory.FIXED_INCOME) }
                        entry<VariableIncomeAssetRouting> { screen(InvestmentCategory.VARIABLE_INCOME) }
                        entry<FundsAssetRouting> { screen(InvestmentCategory.INVESTMENT_FUND) }
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
        colors = if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) IconButtonDefaults.filledTonalIconButtonColors() else IconButtonDefaults.filledIconButtonColors()
    ) {
        if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        else
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun AssetsScreen(
    modifier: Modifier = Modifier,
    state: AssetsViewModel.AssetsState,
    category: InvestmentCategory,
    onRowClick: (Long) -> Unit,
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
) {

    DataTable(
        columns = when (category) {

            InvestmentCategory.FIXED_INCOME -> listOf(
//                TableColumn(title = "Categoria", data = { category }),
                TableColumn(title = "Subcategoria", data = { subCategory }),
                TableColumn(title = "Descrição", data = { name }, weight = 2f),
                TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity.formated() }, cellContent = { asset ->
                    DateComponent(asset.maturity.formated()) { value -> onIntent(AssetsViewModel.UpdateMaturity(asset.asset, MaturityDate(value))) }
                }),
                TableColumn(title = "Emissor", data = { issuer }),
                TableColumn(title = "Liquidez", data = { liquidity }, cellContent = { asset ->
                    SelectComponent(value = (asset.asset as FixedIncomeAsset).liquidity, options = Liquidity.entries, format = { it.formated() }) {
                        onIntent(AssetsViewModel.UpdateLiquidity(asset.asset, it))
                    }
                }),
                TableColumn(title = "Observação", data = { notes }, weight = 2f, cellContent = { asset ->
                    TextComponent(asset.notes) { value -> onIntent(AssetsViewModel.UpdateDescription(asset.asset, value)) }
                })
            )

            InvestmentCategory.VARIABLE_INCOME -> listOf(
//                TableColumn(title = "Categoria", data = { category }),
                TableColumn(title = "Subcategoria", data = { subCategory }),
                TableColumn(title = "Ticker", data = { name }),
                TableColumn(title = "Emissor", data = { "" }, weight = 2f),
                TableColumn(title = "CNPJ", data = { "" }),
//                TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity.formated() }),
//                TableColumn(title = "Emissor", data = { issuer }),
//                TableColumn(title = "Liquidez", data = { liquidity }),
                TableColumn(title = "Observação", data = { notes }, weight = 2f)
            )

            InvestmentCategory.INVESTMENT_FUND -> listOf(
//                TableColumn(title = "Categoria", data = { category }),
                TableColumn(title = "Subcategoria", data = { subCategory }),
                TableColumn(title = "Descrição", data = { name }, weight = 2f),
//                TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity.formated() }),
                TableColumn(title = "Emissor", data = { issuer }),
//                TableColumn(title = "Liquidez", data = { liquidity }),
                TableColumn(title = "Observação", data = { notes }, weight = 2f)
            )
        },
        data = state.list.map { AssetView.create(it) },
        onRowClick = { view -> onRowClick(view.id) },
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Composable
private fun RowScope.DateComponent(
    value: String,
    onChange: (String) -> Unit,
) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputDate(
        value = value,
        isError = isError,
        onValueChange = {
            setValue(it)
            setError(false)
            runCatching { onChange(it) }.getOrElse { setError(true) }
        }
    )
}


@Composable
private fun RowScope.TextComponent(
    value: String,
    onChange: (String) -> Unit,
) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputText(
        value = value,
        isError = isError,
        onValueChange = {
            setValue(it)
            setError(false)
            runCatching { onChange(it) }.getOrElse { setError(true) }
        }
    )
}

@Composable
private fun <T> RowScope.SelectComponent(
    value: T,
    options: List<T>,
    format: (T) -> String,
    onChange: (T) -> Unit,
) {

    val (value, setValue) = remember(value) { mutableStateOf(value) }
    val (isError, setError) = remember { mutableStateOf(false) }

    TableInputSelect(
        value = value,
        isError = isError,
        onValueChange = {
            setValue(it)
            setError(false)
            runCatching { onChange(it) }.getOrElse { setError(true) }
        },
        options = options,
        format = format
    )
}

//private fun String.toDate() =
//    LocalDate.Format{
//        year();  monthNumber(); day()
//    }.parse(this)