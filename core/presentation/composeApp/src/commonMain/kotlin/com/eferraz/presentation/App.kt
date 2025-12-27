package com.eferraz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
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
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.assets.AssetsRoute
import com.eferraz.presentation.features.history.HistoryRoute
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.KoinConfiguration

@Composable
public fun InternalApp(config: KoinConfiguration) {

    AppTheme {

        @OptIn(KoinExperimentalAPI::class)
        KoinMultiplatformApplication(config = config) {

            AppNavigationHost()
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavigationHost() {

    val backStack = rememberNavBackStack(config, AssetsRouting)

    NavigationSuiteScaffold(
        navigationItems = menus(backStack),
        navigationItemVerticalArrangement = Arrangement.Center,
        content = appNavDisplay(backStack),
//        primaryActionContent = primaryActionContent,
    )
}

private fun menus(backStack: NavBackStack<NavKey>): @Composable () -> Unit = {

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
        label = { Text("Ativos") },
        selected = backStack.lastOrNull() == AssetsRouting,
        onClick = {
            backStack.clear()
            backStack.add(AssetsRouting)
        }
    )

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
        label = { Text("Histórico") },
        selected = backStack.lastOrNull() == HistoryRouting,
        onClick = {
            backStack.clear()
            backStack.add(HistoryRouting)
        }
    )
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