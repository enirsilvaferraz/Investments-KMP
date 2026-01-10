package com.eferraz.presentation.features.assets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.value.MaturityDate
import com.eferraz.presentation.design_system.components.inputs.TableInputDate
import com.eferraz.presentation.design_system.components.inputs.TableInputSelect
import com.eferraz.presentation.design_system.components.inputs.TableInputText
import com.eferraz.presentation.design_system.components.new_table.UiTable
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateAsset
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsIntent.UpdateBrokerage
import com.eferraz.presentation.features.assets.AssetsViewModel.AssetsState
import com.eferraz.presentation.helpers.Formatters.formated

@Composable
internal fun AssetsScreenFixedIncome(
    modifier: Modifier = Modifier,
    state: AssetsState,
    onRowClick: (Long) -> Unit = {},
    onIntent: (AssetsIntent) -> Unit,
) {
    UiTable(
        modifier = modifier,
        data = state.list.filterIsInstance<FixedIncomeAsset>(),
        onSelect = { asset -> onRowClick(asset.id) }
    ) {
        column(
            header = "Corretora",
            sortedBy = { state.assetBrokerages[it.id]?.name.orEmpty() },
            cellContent = { asset ->
                TableInputSelect(
                    value = state.assetBrokerages[asset.id],
                    options = listOf(null) + state.brokerages,
                    format = { it?.name.orEmpty() },
                    onChange = { value -> onIntent(UpdateBrokerage(asset.id, value)) }
                )
            }
        )

        column(
            header = "SubCategoria",
            sortedBy = { it.subType },
            cellContent = { asset ->
                TableInputSelect(
                    value = asset.subType,
                    options = FixedIncomeSubType.entries,
                    format = { it.formated() },
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(subType = value))) }
                )
            }
        )

        column(
            header = "Tipo",
            sortedBy = { it.type },
            cellContent = { asset ->
                TableInputSelect(
                    value = asset.type,
                    options = FixedIncomeAssetType.entries,
                    format = { it.formated() },
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(type = value))) }
                )
            }
        )

        column(
            header = "Vencimento",
            sortedBy = { it.expirationDate },
            cellContent = { asset ->
                TableInputDate(
                    value = asset.expirationDate.formated(),
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(expirationDate = MaturityDate(value).get()))) }
                )
            }
        )

        column(
            header = "Taxa",
            sortedBy = { it.contractedYield },
            cellContent = { asset ->
                TableInputText(
                    value = asset.contractedYield.toString(),
                    onChange = { value -> value.toDoubleOrNull()?.let { onIntent(UpdateAsset(asset.copy(contractedYield = it))) } }
                )
            }
        )

        column(
            header = "% CDI",
            sortedBy = { it.cdiRelativeYield ?: 0.0 },
            cellContent = { asset ->
                TableInputText(
                    value = asset.cdiRelativeYield?.toString().orEmpty(),
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(cdiRelativeYield = value.toDoubleOrNull()))) }
                )
            }
        )

        column(
            header = "Emissor",
            sortedBy = { it.issuer.name },
            cellContent = { asset ->
                TableInputSelect(
                    value = asset.issuer,
                    options = state.issuers,
                    format = { it.name },
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(issuer = value))) }
                )
            }
        )

        column(
            header = "Liquidez",
            sortedBy = { it.liquidity },
            cellContent = { asset ->
                TableInputSelect(
                    value = asset.liquidity,
                    options = Liquidity.entries,
                    format = { it.formated() },
                    onChange = { value -> onIntent(UpdateAsset(asset.copy(liquidity = value))) }
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

