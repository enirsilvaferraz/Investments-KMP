package com.eferraz.design_system.scaffolds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.components.segmented_control.SegmentedControl


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun AppScreenScaffold(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    mainPane: @Composable () -> Unit = {},
    subMainPane: (@Composable () -> Unit)? = null,
    supportingPane: (@Composable ColumnScope.() -> Unit)? = null,
    extraPane: (@Composable ColumnScope.() -> Unit)? = null,
) {

    val cornerPadding = 24

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest).padding(top = 12.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        content = actions
                    )
                }
            )
        }
    ) {

        Column(modifier = Modifier.padding(it)) {

            val navigator: ThreePaneScaffoldNavigator<Nothing> = rememberSupportingPaneScaffoldNavigator<Nothing>()

            BoxWithConstraints {

                SupportingPaneScaffold(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest).padding(end = cornerPadding.dp),
                    directive = navigator.scaffoldDirective.copy(
                        horizontalPartitionSpacerSize = cornerPadding.dp,
                        defaultPanePreferredWidth = maxWidth * 0.22f
                    ),
                    value = navigator.scaffoldValue,
                    mainPane = {

                        AnimatedPane(modifier = Modifier.safeContentPadding()) {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = cornerPadding.dp)
                            ) {

                                Surface(
                                    modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    content = mainPane
                                )

                                subMainPane?.invoke()
                            }
                        }
                    },
                    supportingPane = {

                        supportingPane?.let {

                            AnimatedPane(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    content = supportingPane
                                )
                            }
                        }
                    },
                    extraPane = {

                        extraPane?.let {

                            AnimatedPane(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                                Column(content = extraPane)
                            }
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Preview(widthDp = 2000, heightDp = 1000)
@Preview(widthDp = 1300, heightDp = 600)
@Composable
public fun AppScaffoldPreview() {
    MaterialTheme {
        ApplicationScaffold()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ApplicationScaffold() {

    NavigationSuiteScaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
        navigationItemVerticalArrangement = Arrangement.Center,
        primaryActionContent = primaryActionContent,
        navigationItems = @Composable {
            for (i in 0..<5) {
                NavigationSuiteItem(
                    icon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Ativos") },
                    label = { Text("Ativos") },
                    selected = i == 2,
                    onClick = { }
                )
            }
        }
    ) {

        AppScreenScaffold(
            title = "Titulo da Tela",
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu, contentDescription = "Ativos"
                    )
                }
            },
            supportingPane = {

                AppScreenPane {

                    var r1 by remember { mutableStateOf("Renda Variável") }

                    SegmentedControl(
                        selected = r1,
                        options = listOf("Renda Fixa", "Renda Variável", "Fundos"),
                        onSelect = { r1 = it },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )

                    var r2 by remember { mutableStateOf("Liquidez Diária") }

                    SegmentedControl(
                        selected = r2,
                        options = listOf("Liquidez Diária", "No Vencimento"),
                        onSelect = { r2 = it },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }

                AppScreenPane(
                    modifier = Modifier.height(300.dp),
                    color = Color.Blue.copy(alpha = 0.3f)
                ) {}

                AppScreenPane(
                    modifier = Modifier.height(300.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {}

                AppScreenPane(
                    modifier = Modifier.padding(bottom = 16.dp).height(300.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {}
            },
            subMainPane = {

                var r1 by remember { mutableStateOf("Nubank") }

                SegmentedControl(
                    selected = r1,
                    options = listOf("Nubank", "Bradesco", "Santander", "Itaú", "Banco do Brasil", "Inter"),
                    onSelect = { r1 = it }
                )
            }
        )
    }
}

private val primaryActionContent: @Composable () -> Unit = {

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp).padding(top = 25.dp)) {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {

            FloatingActionButton(
                onClick = { /* TODO: Implementar ação futura */ },
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