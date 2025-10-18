package com.eferraz.pokedex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eferraz.pokedex.utils.shouldUseNavRail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun HomeRoute(modifier: Modifier = Modifier) {


    val useNavRail = shouldUseNavRail()

    val destinations = listOf("A", "B", "C")
    var selected by remember { mutableStateOf(destinations[0]) }

    if (useNavRail) {
        Row(horizontalArrangement = Arrangement.Center) {
            NavigationRail(windowInsets = NavigationRailDefaults.windowInsets) {
                Spacer(Modifier.weight(1f))
                destinations.forEach { item ->
                    NavigationRailItem(
                        selected = selected == item,
                        onClick = { selected = item },
                        icon = {
                            Icon(Icons.Default.Home, item)
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            SupportingPane(useNavRail)
        }
    } else {
        Column(verticalArrangement = Arrangement.Bottom) {

            BottomAppBar {
                destinations.forEach { item ->
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(Icons.Default.Home, item)
                    }
                }
            }

            SupportingPane(useNavRail)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun SupportingPane(useNavRail: Boolean) {

    val scope = rememberCoroutineScope()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

    SupportingPaneScaffold(
        modifier = Modifier.alpha(1f).padding(24.dp),
        directive = navigator.scaffoldDirective.copy(horizontalPartitionSpacerSize = 24.dp),
        value = navigator.scaffoldValue,
        mainPane = {
            AnimatedPane(modifier = Modifier.safeContentPadding()) {
                MainPane {
                    scope.launch {
                        navigator.navigateTo(SupportingPaneScaffoldRole.Extra)
                    }
                }
            }
        },
        supportingPane = {},
        extraPane = {
            AnimatedPane(modifier = Modifier.safeContentPadding()) {

            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
private fun MainPane(
    onItemSelected: (data: FixedIncomeInvestment) -> Unit,
) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.TopStart
    ) {

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Investimentos", style = MaterialTheme.typography.headlineLarge) })
            }
        ) {

            FixedIncomeDataTable(modifier = Modifier.padding(it)) {
//                onItemSelected(it)
            }
        }
    }
}