package com.eferraz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.assets.AssetsRoute
import com.eferraz.presentation.features.history.HistoryRoute
import org.jetbrains.compose.ui.tooling.preview.Preview
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

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeSelected = currentRoute?.contains("AssetsRouting", ignoreCase = true) == true
    val isHistorySelected = currentRoute?.contains("HistoryRouting", ignoreCase = true) == true

    NavigationSuiteScaffold(
        navigationItems = {
            NavigationSuiteItem(
                icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
                label = { Text("Ativos") },
                selected = isHomeSelected,
                onClick = { navigateTo(navController, AssetsRouting) }
            )
            NavigationSuiteItem(
                icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
                label = { Text("Histórico") },
                selected = isHistorySelected,
                onClick = { navigateTo(navController, HistoryRouting) }
            )
        },
        navigationItemVerticalArrangement = Arrangement.Center,
//        primaryActionContent = {
//
//            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).padding(top = 25.dp)) {
//
//                AnimatedVisibility(
//                    visible = isHomeSelected,
//                    enter = fadeIn() + scaleIn(),
//                    exit = fadeOut() + scaleOut()
//                ) {
//
//                    FloatingActionButton(
////                        modifier = Modifier.padding(top = 30.dp, start=20.dp),
//                        onClick = { /* TODO: Implementar ação futura */ },
//                        elevation = FloatingActionButtonDefaults.loweredElevation()
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Adicionar"
//                        )
//                    }
//                }
//            }
//        },
        content = {

            NavHost(
                navController = navController,
                startDestination = HistoryRouting,
                modifier = Modifier.fillMaxSize()
            ) {

                composable<AssetsRouting> {
                    AssetsRoute()
                }

                composable<HistoryRouting> {
                    HistoryRoute()
                }
            }
        }
    )
}

private fun navigateTo(navController: NavHostController, route: Any) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}