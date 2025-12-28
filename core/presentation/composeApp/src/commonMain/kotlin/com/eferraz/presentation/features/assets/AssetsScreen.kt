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
import androidx.compose.material3.Surface
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
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
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
import com.eferraz.presentation.design_system.components.table.inputDateColumn
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
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

                inputSelectColumn(
                    title = "Subcategoria",
                    getValue = { (it.asset as FixedIncomeAsset).subType },
                    options = FixedIncomeSubType.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(subType = value)))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).subType }
                ),

                inputSelectColumn(
                    title = "Tipo",
                    getValue = { (it.asset as FixedIncomeAsset).type },
                    options = FixedIncomeAssetType.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(type = value)))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).type }
                ),

                inputDateColumn(
                    title = "Vencimento",
                    getValue = { (it.asset as FixedIncomeAsset).expirationDate.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(expirationDate = MaturityDate(value).get())))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).expirationDate }
                ),

                inputTextColumn(
                    title = "Taxa",
                    getValue = { (it.asset as FixedIncomeAsset).contractedYield.toString() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        value.toDoubleOrNull()?.let { onIntent(AssetsViewModel.UpdateAsset(asset.copy(contractedYield = it))) }
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).contractedYield }
                ),

                inputTextColumn(
                    title = "% CDI",
                    getValue = { (it.asset as FixedIncomeAsset).cdiRelativeYield?.toString() ?: "" },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        val d = value.toDoubleOrNull()
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(cdiRelativeYield = d)))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).cdiRelativeYield }
                ),

                inputSelectColumn(
                    title = "Emissor",
                    getValue = { (it.asset as FixedIncomeAsset).issuer },
                    options = state.issuers,
                    format = { it.name },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(issuer = value)))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).issuer.name }
                ),

                inputSelectColumn(
                    title = "Liquidez",
                    getValue = { (it.asset as FixedIncomeAsset).liquidity },
                    options = Liquidity.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(liquidity = value)))
                    },
                    sortableValue = { (it.asset as FixedIncomeAsset).liquidity }
                ),

                inputTextColumn(
                    title = "Observação",
                    getValue = { (it.asset as FixedIncomeAsset).observations.orEmpty() },
                    onValueChange = { view, value ->
                        val asset = view.asset as FixedIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(observations = value)))
                    },
                    weight = 2f,
                    sortableValue = { (it.asset as FixedIncomeAsset).observations }
                )
            )

            InvestmentCategory.VARIABLE_INCOME -> listOf(
                inputSelectColumn(
                    title = "Tipo",
                    getValue = { (it.asset as VariableIncomeAsset).type },
                    options = VariableIncomeAssetType.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as VariableIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(type = value)))
                    },
                    sortableValue = { (it.asset as VariableIncomeAsset).type }
                ),
                inputTextColumn(
                    title = "Ticker",
                    getValue = { (it.asset as VariableIncomeAsset).ticker },
                    onValueChange = { view, value ->
                        val asset = view.asset as VariableIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(ticker = value)))
                    },
                    sortableValue = { (it.asset as VariableIncomeAsset).ticker }
                ),
                inputTextColumn(
                    title = "Nome",
                    getValue = { (it.asset as VariableIncomeAsset).name },
                    onValueChange = { view, value ->
                        val asset = view.asset as VariableIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(name = value)))
                    },
                    sortableValue = { (it.asset as VariableIncomeAsset).name }
                ),
                inputSelectColumn(
                    title = "Emissor",
                    getValue = { (it.asset as VariableIncomeAsset).issuer },
                    options = state.issuers,
                    format = { it.name },
                    onValueChange = { view, value ->
                        val asset = view.asset as VariableIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(issuer = value)))
                    },
                    sortableValue = { (it.asset as VariableIncomeAsset).issuer.name }
                ),
                inputTextColumn(
                    title = "Observação",
                    getValue = { (it.asset as VariableIncomeAsset).observations.orEmpty() },
                    onValueChange = { view, value ->
                        val asset = view.asset as VariableIncomeAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(observations = value)))
                    },
                    weight = 2f,
                    sortableValue = { (it.asset as VariableIncomeAsset).observations }
                )
            )

            InvestmentCategory.INVESTMENT_FUND -> listOf(
                inputSelectColumn(
                    title = "Tipo",
                    getValue = { (it.asset as InvestmentFundAsset).type },
                    options = InvestmentFundAssetType.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(type = value)))
                    },
                    sortableValue = { (it.asset as InvestmentFundAsset).type }
                ),
                inputTextColumn(
                    title = "Nome",
                    getValue = { (it.asset as InvestmentFundAsset).name },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(name = value)))
                    },
                    weight = 2f,
                    sortableValue = { (it.asset as InvestmentFundAsset).name }
                ),
                inputSelectColumn(
                    title = "Liquidez",
                    getValue = { (it.asset as InvestmentFundAsset).liquidity },
                    options = Liquidity.entries,
                    format = { it.formated() },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(liquidity = value)))
                    },
                    sortableValue = { (it.asset as InvestmentFundAsset).liquidity }
                ),
                inputTextColumn(
                    title = "Dias Liq.",
                    getValue = { (it.asset as InvestmentFundAsset).liquidityDays.toString() },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        value.toIntOrNull()?.let { onIntent(AssetsViewModel.UpdateAsset(asset.copy(liquidityDays = it))) }
                    },
                    sortableValue = { (it.asset as InvestmentFundAsset).liquidityDays }
                ),
                inputDateColumn(
                    title = "Vencimento",
                    getValue = { (it.asset as InvestmentFundAsset).expirationDate?.formated() ?: "" },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        val date = if (value.isBlank()) null else MaturityDate(value).get()
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(expirationDate = date)))
                    },
                    sortableValue = { (it.asset as InvestmentFundAsset).expirationDate }
                ),
                inputSelectColumn(
                    title = "Emissor",
                    getValue = { (it.asset as InvestmentFundAsset).issuer },
                    options = state.issuers,
                    format = { it.name },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(issuer = value)))
                    },
                    sortableValue = { (it.asset as InvestmentFundAsset).issuer.name }
                ),
                inputTextColumn(
                    title = "Observação",
                    getValue = { (it.asset as InvestmentFundAsset).observations.orEmpty() },
                    onValueChange = { view, value ->
                        val asset = view.asset as InvestmentFundAsset
                        onIntent(AssetsViewModel.UpdateAsset(asset.copy(observations = value)))
                    },
                    weight = 2f,
                    sortableValue = { (it.asset as InvestmentFundAsset).observations }
                )
            )
        },
        data = state.list.map { AssetView.create(it) },
//        onRowClick = { view -> onRowClick(view.id) },
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

@Preview(widthDp = 800, heightDp = 200)
@Composable
private fun AssetsScreen() {

    AppTheme {
        Surface {
            AssetsScreen(
                state = AssetsViewModel.AssetsState(emptyList(), emptyList()),
                category = InvestmentCategory.FIXED_INCOME,
                onRowClick = {},
                onIntent = {}
            )
        }
    }
}