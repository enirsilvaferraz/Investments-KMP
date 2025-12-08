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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.features.assetForm.AssetFormScreen
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import com.eferraz.presentation.helpers.Formatters.formated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {

    val tableVm = koinViewModel<AssetsViewModel>()
    val tableState by tableVm.state.collectAsStateWithLifecycle()

    val formVm = koinViewModel<AssetFormViewModel>()
    val formState by formVm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    // Recarrega assets quando o formulário salva e fecha o painel se necessário
    LaunchedEffect(formState.message, formState.shouldCloseForm) {
        if (formState.message != null) {
            tableVm.loadAssets()
            
            // Fechar o painel se foi salvo com sucesso
            if (formState.shouldCloseForm && navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                navigator.navigateBack()
                // Resetar o flag após usar
                formVm.processIntent(AssetFormIntent.ResetCloseFlag)
            }
        }
    }

    AppScaffold(
        title = "Ativos",
        navigator = navigator,
        mainPane = {
            AssetsScreen(
                list = tableState.list.map { AssetView.create(it) },
                onRowClick = { assetId ->
                    scope.launch {
                        formVm.processIntent(AssetFormIntent.LoadAssetForEdit(assetId))
                        navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                    }
                }
            )
        },
        actions = {
            AssetsActions(scope, navigator, formVm)
        },
        extraPane = {
            if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                AssetFormScreen(
                    state = formState,
                    onIntent = { formVm.processIntent(it) },
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun AssetsActions(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Nothing>,
    formVm: AssetFormViewModel,
) {

    FilledIconButton(
        onClick = {
            scope.launch {
                if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                    navigator.navigateBack()
                } else {
                    // Limpar formulário para modo de cadastro
                    formVm.processIntent(AssetFormIntent.ClearForm)
                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                }
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
    onRowClick: (Long) -> Unit,
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
        onRowClick = { view -> onRowClick(view.id) }
    )
}