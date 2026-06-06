package com.eferraz.investments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.eferraz.asset_management.assets.AssetManagementDialog
import com.eferraz.asset_management.di.AssetManagementRouting
import com.eferraz.design_system.theme.AppTheme
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.presentation.features.history.HoldingHistoryRoute
import org.koin.plugin.module.dsl.startKoin

@Composable
public fun App() {

    startKoin<MyKoinApp>()

    AppThemeV2(false) {
        AppTheme {
            AppNavigationHost()
        }
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
        primaryActionContent = {
            ActionButton(
                onClick = {
                    pushAssetManagementDialog(backStack)
                }
            )
        },
    )
}

@Suppress("FunctionSignature")
private fun navRailMenus(backStack: NavBackStack<NavKey>): @Composable () -> Unit = {

    NavigationSuiteItem(
        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
        label = { Text("Histórico") },
        selected = backStack.lastOrNull() == HistoryRouting,
        onClick = { backStack[0] = HistoryRouting }
    )
}

@Suppress("FunctionSignature")
private fun appNavDisplay(backStack: NavBackStack<NavKey>): @Composable () -> Unit = {

    val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(dialogStrategy),
        entryProvider = entryProvider {

            entry<HistoryRouting> {
                HoldingHistoryRoute(
                    onEditHolding = { holdingId ->
                        pushAssetManagementDialog(backStack, holdingId)
                    }
                )
            }

            entry<AssetManagementRouting>(
                metadata = DialogSceneStrategy.dialog(
                    DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                        usePlatformDefaultWidth = false
                    )
                )
            ) { routing ->

                AssetManagementDialog(
                    holdingId = routing.holdingId,
                    onDismiss = { backStack.removeLastOrNull() },
                )
            }
        }
    )
}

private fun pushAssetManagementDialog(
    backStack: NavBackStack<NavKey>,
    holdingId: Long? = null,
) {
    val routing = AssetManagementRouting(holdingId = holdingId)
    if (backStack.lastOrNull() is AssetManagementRouting) {
        backStack[backStack.lastIndex] = routing
    } else {
        // Push em vez de substituir — diálogos precisam de conteúdo por baixo (overlaidEntries)
        backStack += routing
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit) {

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).padding(top = 25.dp)) {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {

            FloatingActionButton(
                onClick = onClick,
                elevation = FloatingActionButtonDefaults.loweredElevation()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar"
                )
            }
        }
    }
}
