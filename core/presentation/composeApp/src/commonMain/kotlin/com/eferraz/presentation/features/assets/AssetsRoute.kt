package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
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
import com.eferraz.entities.InvestmentCategory
import com.eferraz.presentation.FixedIncomeAssetRouting
import com.eferraz.presentation.FundsAssetRouting
import com.eferraz.presentation.VariableIncomeAssetRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
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

