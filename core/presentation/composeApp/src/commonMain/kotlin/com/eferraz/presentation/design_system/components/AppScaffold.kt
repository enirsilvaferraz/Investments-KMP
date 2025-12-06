package com.eferraz.presentation.design_system.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.components.panels.Pane
import com.eferraz.presentation.design_system.components.panels.Section


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AppScaffold(
    title: String,
    actions: @Composable () -> Unit = {},
    mainPane: @Composable () -> Unit,
    extraPane: (@Composable () -> Unit)? = null,
    supportingPane: (@Composable () -> Unit)? = null,
) {

    Surface {

        Scaffold(
            modifier = Modifier.padding(top = 12.dp),
            topBar = {
                TopAppBar(
                    title = { Text(title, modifier = Modifier.offset(x = (-12).dp)) },
                    actions = { actions() }
                )
            }
        ) {

            Surface {

                val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

                SupportingPaneScaffold(
                    modifier = Modifier.padding(it).padding(end = 32.dp),
                    directive = navigator.scaffoldDirective.copy(horizontalPartitionSpacerSize = 24.dp),
                    value = navigator.scaffoldValue,
                    mainPane = {
                        Pane {
                            Section(Modifier.fillMaxSize()) {
                                mainPane()
                            }
                        }
                    },
                    supportingPane = {
                        supportingPane?.let { panel ->
                            Pane {
                                Section(Modifier.fillMaxSize()) {
                                    panel()
                                }
                            }
                        }
                    },
                    extraPane = {
                        extraPane?.let { panel ->
                            Pane {
                                Section(Modifier.fillMaxSize()) {
                                    panel()
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}