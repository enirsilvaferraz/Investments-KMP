package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.value.CNPJ
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.design_system.components.table.textColumn
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateAsset
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateBrokerage
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsState
import com.eferraz.presentation.helpers.Formatters.formated

@Composable
internal fun AssetsScreenVariableIncome(
    modifier: Modifier = Modifier,
    state: AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsIntent) -> Unit,
) {
    DataTable(
        data = state.list.filterIsInstance<VariableIncomeAsset>(),
        columns = listOf(

            inputSelectColumn(
                title = "Corretora",
                sortableValue = { state.assetBrokerages[it.id]?.name.orEmpty() },
                getValue = { state.assetBrokerages[it.id] },
                format = { it?.name.orEmpty() },
                options = listOf(null) + state.brokerages,
                onValueChange = { asset, value -> onIntent(UpdateBrokerage(asset.id, value)) }
            ),

            inputSelectColumn(
                title = "Tipo",
                sortableValue = { it.type },
                getValue = { it.type },
                options = VariableIncomeAssetType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) }
            ),

            inputTextColumn(
                title = "Ticker",
                sortableValue = { it.ticker },
                getValue = { it.ticker },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(ticker = value))) }
            ),

            inputTextColumn(
                title = "CNPJ",
                sortableValue = { it.cnpj?.get() },
                getValue = { it.cnpj?.get() ?: "" },
                onValueChange = { asset, value ->
                    val cnpj = if (value.isBlank()) {
                        null
                    } else {
                        try {
                            CNPJ(value)
                        } catch (e: IllegalArgumentException) {
                            // Se o CNPJ for inválido, mantém o valor anterior
                            return@inputTextColumn
                        }
                    }
                    onIntent(UpdateAsset(asset.copy(cnpj = cnpj)))
                }
            ),

            textColumn(
                title = "Nome",
                weight = 2f,
                getValue = { it.name },
            ),

            inputTextColumn(
                title = "Observação",
                sortableValue = { it.observations },
                getValue = { it.observations.orEmpty() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(observations = value))) },
                weight = 2f
            )
        ),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

