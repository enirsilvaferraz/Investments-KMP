package com.eferraz.presentation.assets

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.pokedex.ui.components.DataTable
import com.eferraz.pokedex.ui.components.TableColumn
import com.eferraz.presentation.assets.Formatters.formated
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
                Pane {
                    Section(Modifier.fillMaxSize()) {}
                }
            },
            extraPane = {
                Pane {
                    Section(Modifier.fillMaxSize()) {}
                }
            },
        )
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldPaneScope.Pane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedPane(modifier = Modifier.safeContentPadding()) {
        Column(verticalArrangement = spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun Section(modifier: Modifier = Modifier, content: @Composable (() -> Unit)) {
    Surface(modifier = modifier.clip(RoundedCornerShape(12.dp)), content = content)
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
            TableColumn(title = "Observação", weight = 2f, extractValue = { it.notes })
        ),
        data = list,
    )
}