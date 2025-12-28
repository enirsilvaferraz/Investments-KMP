package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.features.assets.AssetsIntent.UpdateAsset
import com.eferraz.presentation.features.assets.AssetsIntent.UpdateBrokerage
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
                title = "Nome",
                sortableValue = { it.name },
                getValue = { it.name },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(name = value))) }
            ),

            inputSelectColumn(
                title = "Emissor",
                sortableValue = { it.issuer.name },
                getValue = { it.issuer },
                format = { it.name },
                options = state.issuers,
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(issuer = value))) }
            ),

            inputTextColumn(
                title = "Observação",
                sortableValue = { it.observations },
                getValue = { it.observations.orEmpty() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(observations = value))) },
                weight = 2f
            ),

            inputSelectColumn(
                title = "Corretora",
                sortableValue = { state.assetBrokerages[it.id]?.name.orEmpty() },
                getValue = { state.assetBrokerages[it.id] },
                format = { it?.name.orEmpty() },
                options = listOf(null) + state.brokerages,
                onValueChange = { asset, value -> onIntent(UpdateBrokerage(asset.id, value)) }
            )
        ),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

