package com.eferraz.presentation.assets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.assets.Formatters.formated
import com.eferraz.usecases.repositories.AssetRepository
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.TableColumnWidth.Flex
import com.seanproctor.datatable.material3.DataTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val repository: AssetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsState(emptyList()))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { AssetsState(repository.getAll()) }
        }
    }

    data class AssetsState(
        val list: List<Asset>,
    )
}

internal class AssetView(
    val category: String,
    val subCategory: String,
    val name: String,
    val maturity: LocalDate?,
    val issuer: String,
    val notes: String,
) {

    companion object {

        @Composable
        fun create(asset: Asset) = AssetView(
            category = when (asset) {
                is FixedIncomeAsset -> "Renda Fixa"
                is InvestmentFundAsset -> "Renda Variável"
                is VariableIncomeAsset -> "Renda Variável"
            },
            subCategory = when (asset) {
                is FixedIncomeAsset -> asset.type.formated()
                is InvestmentFundAsset -> asset.type.formated()
                is VariableIncomeAsset -> asset.type.formated()
            },
            name = when (asset) {
                is FixedIncomeAsset -> when (asset.type) {
                    FixedIncomeAssetType.POST_FIXED -> "${asset.subType.name} de ${asset.contractedYield}% do CDI"
                    FixedIncomeAssetType.PRE_FIXED -> "${asset.subType.name} de $${asset.contractedYield}% a.a."
                    FixedIncomeAssetType.INFLATION_LINKED -> "${asset.subType.name} + ${asset.contractedYield}%"
                }

                is InvestmentFundAsset -> asset.name
                is VariableIncomeAsset -> asset.name
            },
            maturity = when (asset) {
                is FixedIncomeAsset -> asset.expirationDate
                is InvestmentFundAsset -> asset.expirationDate
                is VariableIncomeAsset -> null
            },
            issuer = asset.issuer.name,
            notes = asset.observations.orEmpty(),
        )
    }
}

@Composable
internal fun AssetsRoute() {

    val vm = koinViewModel<AssetsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    AssetsScreen(state.list.map { AssetView.create(it) })
}

@Composable
internal fun AssetsScreen(list: List<AssetView>) {

    var list by remember(list) { mutableStateOf(list) }

    var columnIndex by remember { mutableStateOf<Int?>(null) }
    var columnAscending by remember { mutableStateOf(true) }

    val colorEven = MaterialTheme.colorScheme.surfaceBright
    val colorOdd = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.3f)

    fun onSort(onItem: (it: AssetView) -> Comparable<*>?): (Int, Boolean) -> Unit = { index: Int, ascending: Boolean ->

        columnIndex = index
        columnAscending = ascending

        list = list.let {
            if (ascending) it.sortedWith(compareBy { onItem(it) })
            else it.sortedWith(compareByDescending { onItem(it) })
        }
    }

    DataTable(
        modifier = Modifier.fillMaxSize(),
        columns = listOf(
            DataColumn(Alignment.Center, Flex(1f), onSort { it.category }) { Text("Categoria") },
            DataColumn(Alignment.Center, Flex(1f), onSort { it.subCategory }) { Text("Subcategoria") },
            DataColumn(Alignment.Center, Flex(1f), onSort { it.name }) { Text("Descrição") },
            DataColumn(Alignment.Center, Flex(1f), onSort { it.maturity }) { Text("Vencimento") },
            DataColumn(Alignment.Center, Flex(1f), onSort { it.issuer }) { Text("Emissor") },
            DataColumn(Alignment.Center, Flex(1f), onSort { it.notes }) { Text("Observação") },
        ),
        sortColumnIndex = columnIndex,
        sortAscending = columnAscending
    ) {
        list.forEachIndexed { index: Int, it: AssetView ->
            row {
                backgroundColor = if (index % 2 == 0) colorOdd else colorEven
                cell { Text(it.category) }
                cell { Text(it.subCategory) }
                cell { Text(it.name) }
                cell { Text(it.maturity.formated()) }
                cell { Text(it.issuer) }
                cell { Text(it.notes) }
            }
        }
    }
}