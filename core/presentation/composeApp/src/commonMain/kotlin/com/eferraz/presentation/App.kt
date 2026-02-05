package com.eferraz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.presentation.design_system.components.new_table.UITablePreview
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.assets.AssetsRoute
import com.eferraz.presentation.features.goals.GoalsMonitoringRoute
import com.eferraz.presentation.features.history.HistoryRoute

@Composable
public fun InternalApp() {

    AppTheme {
        AppNavigationHost()
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavigationHost() {

    val backStack = rememberNavBackStack(config, HistoryRouting)

    NavigationSuiteScaffold(
        navigationItems = navRailMenus(backStack),
        navigationItemVerticalArrangement = Arrangement.Center,
        content = appNavDisplay(backStack),
//        primaryActionContent = primaryActionContent,
    )
}

private fun navRailMenus(backStack: NavBackStack<NavKey>): @Composable () -> Unit = {

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
        label = { Text("Ativos") },
        selected = backStack.lastOrNull() == AssetsRouting,
        onClick = { backStack[0] = AssetsRouting }
    )

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
        label = { Text("Histórico") },
        selected = backStack.lastOrNull() == HistoryRouting,
        onClick = { backStack[0] = HistoryRouting }
    )

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Metas") },
        label = { Text("Metas") },
        selected = backStack.lastOrNull() == GoalsMonitoringRouting,
        onClick = { backStack[0] = GoalsMonitoringRouting }
    )

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.DeveloperMode, contentDescription = "Histórico") },
        label = { Text("Histórico") },
        selected = backStack.lastOrNull() == HistoryRoutingV2,
        onClick = { backStack[0] = HistoryRoutingV2 }
    )

//    NavigationSuiteItem(
//        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Test") },
//        label = { Text("Test") },
//        selected = backStack.lastOrNull() == TestRouting,
//        onClick = { backStack[0] = TestRouting }
//    )
}

private fun appNavDisplay(backStack: NavBackStack<NavKey>): @Composable () -> Unit = {

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {

            entry<AssetsRouting> {
                AssetsRoute()
            }

            entry<HistoryRouting> {
                HistoryRoute()
            }

            entry<GoalsMonitoringRouting> {
                GoalsMonitoringRoute()
            }

            entry<TestRouting> {
                UITablePreview()
            }

            entry<HistoryRoutingV2> {
                HistoryRoute(true)
            }
        }
    )
}

//val primaryActionContent: () -> Unit = {
//
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).padding(top = 25.dp)) {
//
//        AnimatedVisibility(
//            visible = isHomeSelected,
//            enter = fadeIn() + scaleIn(),
//            exit = fadeOut() + scaleOut()
//        ) {
//
//            FloatingActionButton(
////                        modifier = Modifier.padding(top = 30.dp, start=20.dp),
//                onClick = { /* TODO: Implementar ação futura */ },
//                elevation = FloatingActionButtonDefaults.loweredElevation()
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Adicionar"
//                )
//            }
//        }
//    }
//}