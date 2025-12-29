package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.value.MaturityDate
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputDateColumn
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateAsset
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateBrokerage
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsState
import com.eferraz.presentation.helpers.Formatters.formated

@Composable
internal fun AssetsScreenFunds(
    modifier: Modifier = Modifier,
    state: AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsIntent) -> Unit,
) {
    DataTable(
        data = state.list.filterIsInstance<InvestmentFundAsset>(),
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
                options = InvestmentFundAssetType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) }
            ),

            inputTextColumn(
                title = "Nome",
                sortableValue = { it.name },
                getValue = { it.name },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(name = value))) },
                weight = 2f
            ),

            inputSelectColumn(
                title = "Liquidez",
                sortableValue = { it.liquidity },
                getValue = { it.liquidity },
                options = Liquidity.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(liquidity = value))) }
            ),

            inputTextColumn(
                title = "Dias Liq.",
                sortableValue = { it.liquidityDays },
                getValue = { it.liquidityDays.toString() },
                onValueChange = { asset, value -> value.toIntOrNull()?.let { onIntent(UpdateAsset(asset.copy(liquidityDays = it))) } }
            ),

            inputDateColumn(
                title = "Vencimento",
                sortableValue = { it.expirationDate },
                getValue = { it.expirationDate?.formated() ?: "" },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(expirationDate = MaturityDate(value).get()))) }
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
            )
        ),
        contentPadding = PaddingValues(bottom = 70.dp)
    )
}

