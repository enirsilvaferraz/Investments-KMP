package com.eferraz.presentation.features.assets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {

    val vm = koinViewModel<AssetsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Ativos",
        navigator = navigator,
        mainPane = {
            AssetsScreen(list = state.list.map { AssetView.create(it) })
        },
        actions = {
            AssetsActions(scope, navigator)
        },
        extraPane = {}
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun AssetsActions(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Nothing>,
) {

    FilledIconButton(
        onClick = {
            scope.launch {
                if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) navigator.navigateBack()
                else navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
            }
        },
        colors = if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) IconButtonDefaults.filledTonalIconButtonColors() else IconButtonDefaults.filledIconButtonColors()
    ) {
        if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        else
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun AssetsScreen(
    modifier: Modifier = Modifier,
    list: List<AssetView>,
) {

    DataTable(
        modifier = modifier,
        columns = listOf(
            TableColumn(title = "Categoria", data = { category }),
            TableColumn(title = "Subcategoria", data = { subCategory }),
            TableColumn(title = "Descrição", data = { name }, weight = 2f),
            TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity.formated() }),
            TableColumn(title = "Emissor", data = { issuer }),
            TableColumn(title = "Liquidez", data = { liquidity }),
            TableColumn(title = "Observação", data = { notes }, weight = 2f)
        ),
        data = list,
    )
}