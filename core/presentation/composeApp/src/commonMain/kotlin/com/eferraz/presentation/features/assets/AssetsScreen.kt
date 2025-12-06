package com.eferraz.presentation.features.assets

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.DataTable
import com.eferraz.presentation.design_system.components.TableColumn
import com.eferraz.presentation.helpers.Formatters.formated
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetsRoute() {

    val vm = koinViewModel<AssetsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    AppScaffold(
        title = "Ativos",
        mainPane = {
            AssetsScreen(list = state.list.map { AssetView.create(it) })
        }
    )
}

@Composable
internal fun AssetsScreen(
    modifier: Modifier = Modifier,
    list: List<AssetView>,
) {

    DataTable(
        modifier = modifier,
        columns = listOf(
            TableColumn(title = "Categoria", data = { category }),
            TableColumn(title = "Subcategoria", data = { subCategory }),
            TableColumn(title = "Descrição", data = { name }, weight = 2f),
            TableColumn(title = "Vencimento", data = { maturity }, formated = { maturity?.formated() ?: "-" }),
            TableColumn(title = "Emissor", data = { issuer }),
            TableColumn(title = "Liquidez", data = { liquidity }),
            TableColumn(title = "Observação", data = { notes }, weight = 2f)
        ),
        data = list,
    )
}