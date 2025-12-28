package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.value.MaturityDate
import com.eferraz.presentation.design_system.components.table.DataTable
import com.eferraz.presentation.design_system.components.table.inputDateColumn
import com.eferraz.presentation.design_system.components.table.inputSelectColumn
import com.eferraz.presentation.design_system.components.table.inputTextColumn
import com.eferraz.presentation.features.assets.AssetsIntent.UpdateAsset
import com.eferraz.presentation.helpers.Formatters.formated

@Composable
internal fun AssetsScreenFixedIncome(
    modifier: Modifier = Modifier,
    state: AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsIntent) -> Unit,
) {
    DataTable(
        data = state.list.filterIsInstance<FixedIncomeAsset>(),
        columns = listOf(

            inputSelectColumn(
                title = "SubCategoria",
                sortableValue = { it.subType },
                getValue = { it.subType },
                options = FixedIncomeSubType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(subType = value))) }
            ),

            inputSelectColumn(
                title = "Tipo",
                sortableValue = { it.type },
                getValue = { it.type },
                options = FixedIncomeAssetType.entries,
                format = { it.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(type = value))) },
            ),

            inputDateColumn(
                title = "Vencimento",
                sortableValue = { it.expirationDate },
                getValue = { it.expirationDate.formated() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(expirationDate = MaturityDate(value).get()))) },
            ),

            inputTextColumn(
                title = "Taxa",
                sortableValue = { it.contractedYield },
                getValue = { it.contractedYield.toString() },
                onValueChange = { asset, value -> value.toDoubleOrNull()?.let { onIntent(UpdateAsset(asset.copy(contractedYield = it))) } }
            ),

            inputTextColumn(
                title = "% CDI",
                sortableValue = { it.cdiRelativeYield },
                getValue = { it.cdiRelativeYield?.toString().orEmpty() },
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(cdiRelativeYield = value.toDoubleOrNull()))) }
            ),

            inputSelectColumn(
                title = "Emissor",
                sortableValue = { it.issuer.name },
                getValue = { it.issuer },
                format = { it.name },
                options = state.issuers,
                onValueChange = { asset, value -> onIntent(UpdateAsset(asset.copy(issuer = value))) }
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

