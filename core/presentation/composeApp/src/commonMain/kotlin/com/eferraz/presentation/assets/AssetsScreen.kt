package com.eferraz.presentation.assets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.pokedex.ui.components.DataTable
import com.eferraz.pokedex.ui.components.TableColumn
import com.eferraz.presentation.assets.Formatters.formated
import org.koin.compose.viewmodel.koinViewModel


@Composable
internal fun AssetsRoute() {

    val vm = koinViewModel<AssetsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.padding(32.dp), topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(title = { Text("Ativos") })
        }
    ) {
        AssetsScreen(
            modifier = Modifier.padding(it).fillMaxSize(),
            list = state.list.map { AssetView.create(it) }
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
            TableColumn(title = "Observação", weight = 2f, extractValue = { it.notes })
        ),
        data = list,
    )
}