package com.eferraz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.assets.AssetsRoute
import com.eferraz.presentation.features.history.HistoryRoute
import com.eferraz.presentation.helpers.shouldUseNavRail
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
public fun InternalApp(config: KoinConfiguration) {

    AppTheme {

        @OptIn(KoinExperimentalAPI::class)
        KoinMultiplatformApplication(config = config) {

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val useNavRail = shouldUseNavRail()

            Row(modifier = Modifier.fillMaxSize()) {

                if (useNavRail) NavigationRail {

                    Column(
                        modifier = Modifier.fillMaxHeight().width(96.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {

                        NavigationRailItem(
                            selected = currentRoute?.contains("HomeRouting", ignoreCase = true) == true,
                            onClick = { navigateTo(navController, HomeRouting) },
                            icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
                            label = { Text("Ativos") }
                        )

                        NavigationRailItem(
                            selected = currentRoute?.contains("HistoryRouting", ignoreCase = true) == true,
                            onClick = { navigateTo(navController, HistoryRouting) },
                            icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
                            label = { Text("Histórico") }
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = HistoryRouting,
                    modifier = Modifier.fillMaxSize()
                ) {

                    composable<HomeRouting> {
                        AssetsRoute()
                    }

                    composable<HistoryRouting> {
                        HistoryRoute()
                    }
                }
            }
        }
    }
}

private fun navigateTo(navController: NavHostController, route: Any) {

    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}