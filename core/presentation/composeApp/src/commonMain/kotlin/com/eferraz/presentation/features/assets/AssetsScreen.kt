package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Issuer
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.value.MaturityDate
import com.eferraz.presentation.FixedIncomeAssetRouting
import com.eferraz.presentation.FundsAssetRouting
import com.eferraz.presentation.VariableIncomeAssetRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.TableColumn
import com.eferraz.presentation.design_system.components.table.inputDateColumn
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import com.eferraz.presentation.features.assets.AssetsViewModel.UpdateAsset
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

//            @Composable
//            @OptIn(ExperimentalMaterial3AdaptiveApi::class)
//            fun screen(category: InvestmentCategory) {
//
//                val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
//                val tableState by tableVm.state.collectAsStateWithLifecycle()
//                tableVm.loadAssets(category)
//
//                AssetsScreen(
//                    category = category,
//                    state = tableState,
//                    onRowClick = { assetId ->
//                        scope.launch {
//                            formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
//                            navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
//                        }
//                    },
//                    onIntent = { tableVm.onIntent(it) }
//                )
//            }

            Box(modifier = Modifier.fillMaxSize()) {

                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {

                        entry<FixedIncomeAssetRouting> {

                            val category = InvestmentCategory.FIXED_INCOME
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenFixedIncome(state = tableState, onIntent = { tableVm.onIntent(it) })
                        }

                        entry<VariableIncomeAssetRouting> {

                            val category = InvestmentCategory.VARIABLE_INCOME
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenVariableIncome(state = tableState, onIntent = { tableVm.onIntent(it) })
                        }
                        entry<FundsAssetRouting> {

                            val category = InvestmentCategory.INVESTMENT_FUND
                            val tableVm = koinViewModel<AssetsViewModel>(key = category.name)
                            val tableState by tableVm.state.collectAsStateWithLifecycle()
                            tableVm.loadAssets(category)

                            AssetsScreenFunds(state = tableState, onIntent = { tableVm.onIntent(it) })
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
        colors = if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) IconButtonDefaults.filledTonalIconButtonColors() else IconButtonDefaults.filledIconButtonColors()
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
    state: AssetsViewModel.AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
) {

    DataTable(
        columns = listOf(

            inputSelectColumn(
                title = "SubCategoria",
                sortableValue = { it.subType },
                getValue = { it.subType },
                options = FixedIncomeSubType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(subType = value))) }
            ),

            inputSelectColumn(
                title = "Tipo",
                sortableValue = { it.type },
                getValue = { it.type },
                options = FixedIncomeAssetType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) },
            ),

            inputDateColumn(
                title = "Vencimento",
                sortableValue = { it.expirationDate },
                getValue = { it.expirationDate.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(expirationDate = MaturityDate(value).get()))) },
            ),

            inputTextColumn(
                title = "Taxa",
                sortableValue = { it.contractedYield },
                getValue = { it.contractedYield.toString() },
                onValueChange = { asset, value -> value.toDoubleOrNull()?.let { onIntent(UpdateAsset(asset.copy(contractedYield = it))) } }
            ),

            inputTextColumn(
                title = "% CDI",
                sortableValue = { it.cdiRelativeYield },
                getValue = { it.cdiRelativeYield?.toString().orEmpty() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(cdiRelativeYield = value.toDoubleOrNull()))) }
            ),

            inputSelectColumn(
                title = "Emissor",
                sortableValue = { it.issuer.name },
                getValue = { it.issuer },
                format = { it.name },
                options = state.issuers,
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(issuer = value))) }
            ),

            inputSelectColumn(
                title = "Liquidez",
                sortableValue = { it.liquidity },
                getValue = { it.liquidity },
                options = Liquidity.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(liquidity = value))) }
            ),

            inputTextColumn(
                title = "Observação",
                sortableValue = { it.observations },
                getValue = { it.observations.orEmpty() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(observations = value))) },
                weight = 2f
            )
        ),
        data = state.list.filterIsInstance<FixedIncomeAsset>(),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Composable
private fun AssetsScreenVariableIncome(
    modifier: Modifier = Modifier,
    state: AssetsViewModel.AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
) {

    DataTable(
        columns = variableIncomeColumns(state.issuers, onIntent),
        data = state.list.filterIsInstance<VariableIncomeAsset>(),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Composable
private fun AssetsScreenFunds(
    modifier: Modifier = Modifier,
    state: AssetsViewModel.AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
) {

    DataTable(
        columns = fundsColumns(state.issuers, onIntent),
        data = state.list.filterIsInstance<InvestmentFundAsset>(),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

private fun <T : Asset> issuerColumn(
    issuers: List<Issuer>,
    onUpdate: (T, Issuer) -> T,
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
): TableColumn<T> = inputSelectColumn(
    title = "Emissor",
    getValue = { it.issuer },
    options = issuers,
    format = { it.name },
    onValueChange = { asset, value ->
        val updatedAsset = onUpdate(asset, value)
        onIntent(UpdateAsset(updatedAsset))
    },
    sortableValue = { it.issuer.name }
)

private fun <T : Asset> notesColumn(
    onUpdate: (T, String) -> T,
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
): TableColumn<T> = inputTextColumn(
    title = "Observação",
    getValue = { it.observations.orEmpty() },
    onValueChange = { asset, value ->
        val updatedAsset = onUpdate(asset, value)
        onIntent(UpdateAsset(updatedAsset))
    },
    weight = 2f,
    sortableValue = { it.observations }
)

private fun variableIncomeColumns(
    issuers: List<Issuer>,
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
): List<TableColumn<VariableIncomeAsset>> = listOf(
    inputSelectColumn(
        title = "Tipo",
        getValue = { it.type },
        options = VariableIncomeAssetType.entries,
        format = { it.formated() },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) },
        sortableValue = { it.type }
    ),
    inputTextColumn(
        title = "Ticker",
        getValue = { it.ticker },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(ticker = value))) },
        sortableValue = { it.ticker }
    ),
    inputTextColumn(
        title = "Nome",
        getValue = { it.name },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(name = value))) },
        sortableValue = { it.name }
    ),
    issuerColumn(issuers, { asset, issuer -> asset.copy(issuer = issuer) }, onIntent),
    notesColumn({ asset, notes -> asset.copy(observations = notes) }, onIntent)
)

private fun fundsColumns(
    issuers: List<Issuer>,
    onIntent: (AssetsViewModel.AssetsIntent) -> Unit,
): List<TableColumn<InvestmentFundAsset>> = listOf(
    inputSelectColumn(
        title = "Tipo",
        getValue = { it.type },
        options = InvestmentFundAssetType.entries,
        format = { it.formated() },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) },
        sortableValue = { it.type }
    ),
    inputTextColumn(
        title = "Nome",
        getValue = { it.name },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(name = value))) },
        weight = 2f,
        sortableValue = { it.name }
    ),
    inputSelectColumn(
        title = "Liquidez",
        getValue = { it.liquidity },
        options = Liquidity.entries,
        format = { it.formated() },
        onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(liquidity = value))) },
        sortableValue = { it.liquidity }
    ),
    inputTextColumn(
        title = "Dias Liq.",
        getValue = { it.liquidityDays.toString() },
        onValueChange = { asset, value ->
            value.toIntOrNull()?.let { onIntent(UpdateAsset(asset.copy(liquidityDays = it))) }
        },
        sortableValue = { it.liquidityDays }
    ),
    inputDateColumn(
        title = "Vencimento",
        getValue = { it.expirationDate?.formated() ?: "" },
        onValueChange = { asset, value ->
            val date = if (value.isBlank()) null else MaturityDate(value).get()
            onIntent(UpdateAsset(asset.copy(expirationDate = date)))
        },
        sortableValue = { it.expirationDate }
    ),
    issuerColumn(issuers, { asset, issuer -> asset.copy(issuer = issuer) }, onIntent),
    notesColumn({ asset, notes -> asset.copy(observations = notes) }, onIntent)
)