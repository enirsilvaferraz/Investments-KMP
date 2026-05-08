package com.eferraz.asset_management.holdings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.assets.AssetManagementEvents
import com.eferraz.asset_management.assets.AssetManagementViewModel
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.asset_management.assets.AssetManagementUiState
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.Brokerage
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HoldingFormView(
    modifier: Modifier = Modifier,
    holdingId: Long?,
) {

    val vm = koinViewModel<HoldingManagementViewModel>()
    val state = vm.state.collectAsState()

    LaunchedEffect(holdingId) {
        vm.dispatch(HoldingManagementEvents.ScreenEntered(assetId = holdingId))
    }

    HoldingFormView(
        modifier = modifier,
        ui = state.value,
        onEvent = vm::dispatch
    )
}


@Composable
private fun HoldingFormView(
    modifier: Modifier,
    ui: AssetManagementUiState,
    onEvent: (AssetManagementEvents) -> Unit,
) {

    StableExposedDropdown(
        modifier = modifier,
        label = BROKERAGE_FIELD_LABEL,
        displayValue = ui.brokerage?.name.orEmpty(),
        options = ui.brokerages,
        itemLabel = Brokerage::name,
        onItemSelect = { brokerage -> onEvent(AssetManagementEvents.BrokerageChanged(brokerage)) },
        error = ui.brokerageError,
        required = true
    )
}

private class HoldingFormPreviewProvider : PreviewParameterProvider<AssetManagementUiState> {
    override val values: Sequence<AssetManagementUiState> = sequenceOf(
        AssetManagementUiState(category = InvestmentCategory.FIXED_INCOME),
    )
}

@Preview
@Composable
private fun HoldingFormViewPreview(
    @PreviewParameter(HoldingFormPreviewProvider::class) ui: AssetManagementUiState,
) {
    MaterialTheme {

        Surface {

            Card(modifier = Modifier.padding(16.dp)) {

                HoldingFormView(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    ui = ui,
                    onEvent = {}
                )
            }
        }
    }
}
