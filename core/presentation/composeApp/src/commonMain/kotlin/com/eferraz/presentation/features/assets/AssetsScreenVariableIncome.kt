package com.eferraz.presentation.features.assets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.value.CNPJ
import com.eferraz.presentation.design_system.components.inputs.TableInputSelect
import com.eferraz.presentation.design_system.components.inputs.TableInputText
import com.eferraz.presentation.design_system.components.new_table.UiTable
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
    UiTable(
        modifier = modifier,
        data = state.list.filterIsInstance<VariableIncomeAsset>(),
        onSelect = { asset -> onRowClick(asset.id) }

    ) {

        column(
            header = "Corretora",
            sortedBy = { state.assetBrokerages[it.id]?.name.orEmpty() },
            cellContent = { asset ->
                println(state.assetBrokerages[asset.id])
                TableInputSelect(
                    value = state.assetBrokerages[asset.id],
                    options = listOf(null) + state.brokerages,
                    format = { it?.name.orEmpty() },
                    onChange = { value -> onIntent(UpdateBrokerage(asset.id, value)) }
                )
            }
        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            cellContent = { asset ->
                TableInputSelect(
                    value = asset.type,
                    options = VariableIncomeAssetType.entries,
                    format = { it.formated() },
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(type = value))) }
                )
            }
        )

        column(
            header = "Ticker",
            sortedBy = { it.ticker },
            cellContent = { asset ->
                TableInputText(
                    value = asset.ticker,
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(ticker = value))) }
                )
            }
        )

        column(
            header = "CNPJ",
            sortedBy = { (it.cnpj?.get().orEmpty()) },
            cellContent = { asset ->
                TableInputText(
                    value = asset.cnpj?.get() ?: "",
                    onChange = { value ->
                        val cnpj = if (value.isBlank()) {
                            null
                        } else {
                            try {
                                CNPJ(value)
                            } catch (e: IllegalArgumentException) {
                                // Se o CNPJ for inválido, mantém o valor anterior
                                return@TableInputText
                            }
                        }
                        onIntent(UpdateAsset(asset.copy(cnpj = cnpj)))
                    }
                )
            }
        )

        column(
            header = "Nome",
            sortedBy = { it.name },
            cellContent = { asset ->
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = asset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        )

        column(
            header = "Observação",
            sortedBy = { it.observations.orEmpty() },
            cellContent = { asset ->
                TableInputText(
                    value = asset.observations.orEmpty(),
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(observations = value))) }
                )
            }
        )
    }
}

