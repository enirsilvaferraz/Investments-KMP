package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.design_system.components.panels.Pane
import com.eferraz.presentation.design_system.components.panels.Section
import com.eferraz.presentation.features.assets.Formatters.formated
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {

    val vm = koinViewModel<AssetsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(24.dp)) {

        Text("Ativos", style = MaterialTheme.typography.headlineLarge)

        val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

        SupportingPaneScaffold(
            modifier = Modifier.alpha(1f).padding(top = 32.dp),
            directive = navigator.scaffoldDirective.copy(horizontalPartitionSpacerSize = 24.dp),
            value = navigator.scaffoldValue,
            mainPane = {
                Pane {
                    Section(Modifier.fillMaxSize()) {
                        AssetsScreen(list = state.list.map { AssetView.create(it) })
                    }
                }
            },
            supportingPane = {
//                Pane {
//                    Section(Modifier.fillMaxSize()) {}
//                }
            },
            extraPane = {
                Pane {
                    Section(Modifier.fillMaxSize()) {}
                }
            },
        )
    }
}

@Composable
internal fun AssetsScreen(
    modifier: Modifier = Modifier,
    list: List<AssetView>,
) {

    DataTable(
        modifier = modifier,
        columns = listOf(
            TableColumn(title = "Categoria", extractValue = { it.category }),
            TableColumn(title = "Subcategoria", extractValue = { it.subCategory }),
            TableColumn(title = "Descrição", weight = 2f, extractValue = { it.name }),
            TableColumn(title = "Vencimento", extractValue = { it.maturity?.formated() ?: "-" }, sortComparator = { it.maturity }),
            TableColumn(title = "Emissor", extractValue = { it.issuer }),
            TableColumn(title = "Liquidez", extractValue = { it.liquidity }),
            TableColumn(title = "Observação", weight = 2f, extractValue = { it.notes })
        ),
        data = list,
    )
}