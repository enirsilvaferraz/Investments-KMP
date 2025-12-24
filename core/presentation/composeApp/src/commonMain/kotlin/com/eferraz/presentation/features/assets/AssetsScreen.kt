package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eferraz.entities.InvestmentCategory
import com.eferraz.presentation.FixedIncomeAssetRouting
import com.eferraz.presentation.FundsAssetRouting
import com.eferraz.presentation.VariableIncomeAssetRouting
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
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

            MaterialTheme.colorScheme

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            currentRoute?.contains("FixedIncomeAssetRouting", ignoreCase = true) == true
            currentRoute?.contains("VariableIncomeAssetRouting", ignoreCase = true) == true
            currentRoute?.contains("FundsAssetRouting", ignoreCase = true) == true

            Box(modifier = Modifier.fillMaxSize()) {

                NavHost(
                    navController = navController,
                    startDestination = FixedIncomeAssetRouting,
                    modifier = Modifier.fillMaxSize()//.weight(1f)
                ) {

                    composable<FixedIncomeAssetRouting> {

                        val tableVm = koinViewModel<AssetsViewModel>(key = "1")
                        val tableState by tableVm.state.collectAsStateWithLifecycle()
                        tableVm.loadAssets(InvestmentCategory.FIXED_INCOME)

                        AssetsScreen(
                            list = tableState.list.map { AssetView.create(it) },
                            onRowClick = { assetId ->
                                scope.launch {
                                    formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
                                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                }
                            }
                        )
                    }

                    composable<VariableIncomeAssetRouting> {

                        val tableVm = koinViewModel<AssetsViewModel>(key = "2")
                        val tableState by tableVm.state.collectAsStateWithLifecycle()
                        tableVm.loadAssets(InvestmentCategory.VARIABLE_INCOME)

                        AssetsScreen(
                            list = tableState.list.map { AssetView.create(it) },
                            onRowClick = { assetId ->
                                scope.launch {
                                    formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
                                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                }
                            }
                        )
                    }

                    composable<FundsAssetRouting> {

                        val tableVm = koinViewModel<AssetsViewModel>(key = "3")
                        val tableState by tableVm.state.collectAsStateWithLifecycle()
                        tableVm.loadAssets(InvestmentCategory.INVESTMENT_FUND)

                        AssetsScreen(
                            list = tableState.list.map { AssetView.create(it) },
                            onRowClick = { assetId ->
                                scope.launch {
                                    formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
                                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                                }
                            }
                        )
                    }
                }

                val selectedCategory by remember(currentRoute) {
                    derivedStateOf {
                        when {
                            currentRoute?.contains("FixedIncomeAssetRouting", ignoreCase = true) == true -> FixedIncomeAssetRouting
                            currentRoute?.contains("VariableIncomeAssetRouting", ignoreCase = true) == true -> VariableIncomeAssetRouting
                            currentRoute?.contains("FundsAssetRouting", ignoreCase = true) == true -> FundsAssetRouting
                            else -> FundsAssetRouting
                        }
                    }
                }

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
                            icon = Icons.Default.TrendingUp,
                            contentDescription = "Renda Variável"
                        ),
                        SegmentedOption(
                            value = FundsAssetRouting,
                            label = "Fundos",
                            icon = Icons.Default.AccountBalance,
                            contentDescription = "Fundos"
                        )
                    ),
                    selectedValue = selectedCategory,
                    onValueChange = { navController.navigate(it) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(6.dp).align(Alignment.BottomStart)
                )

//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.background(colors.background).padding(top = 8.dp).fillMaxWidth()) {
//
//                    FilterChip(
//                        selected = isFixedIncomeSelected,
//                        label = { Text("Renda Fixa") },
//                        onClick = { navController.navigate(FixedIncomeAssetRouting) },
//                        colors = FilterChipDefaults.filterChipColors(
//                            containerColor = if (isFixedIncomeSelected) colors.primaryContainer else colors.surface,
//                            selectedContainerColor = colors.primaryContainer
//                        )
//                    )
//
//                    FilterChip(
//                        selected = isVariableIncomeSelected,
//                        label = { Text("Renda Variável") },
//                        onClick = { navController.navigate(VariableIncomeAssetRouting) },
//                        colors = FilterChipDefaults.filterChipColors(
//                            containerColor = if (isVariableIncomeSelected) colors.primaryContainer else colors.surface,
//                            selectedContainerColor = colors.primaryContainer
//                        )
//                    )
//
//                    FilterChip(
//                        selected = isFundsSelected,
//                        label = { Text("Fundos") },
//                        onClick = { navController.navigate(FundsAssetRouting) },
//                        colors = FilterChipDefaults.filterChipColors(
//                            containerColor = if (isFundsSelected) colors.primaryContainer else colors.surface,
//                            selectedContainerColor = colors.primaryContainer
//                        )
//                    )
//                }
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
    list: List<AssetView>,
    onRowClick: (Long) -> Unit,
) {
    DataTable(
//        modifier = modifier.weight(1f),
        columns = listOf(
            TableColumn(title = "Categoria", data = { category }),
            TableColumn(title = "Subcategoria", data = { subCategory }),
            TableColumn(title = "Descrição", data = { name }, weight = 2f),
            TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity.formated() }),
            TableColumn(title = "Emissor", data = { issuer }),
            TableColumn(title = "Liquidez", data = { liquidity }),
            TableColumn(title = "Observação", data = { notes }, weight = 2f)
        ),
        data = list,
        onRowClick = { view -> onRowClick(view.id) },
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

//private fun String.toDate() =
//    LocalDate.Format{
//        year();  monthNumber(); day()
//    }.parse(this)