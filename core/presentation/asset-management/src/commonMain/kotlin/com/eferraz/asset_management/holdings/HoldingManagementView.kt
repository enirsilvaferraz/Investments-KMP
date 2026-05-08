package com.eferraz.asset_management.holdings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.BROKERAGE_FIELD_LABEL
import com.eferraz.design_system.components.dropdown.StableExposedDropdown
import com.eferraz.entities.holdings.Brokerage
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HoldingFormView(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onComplete: () -> Unit,
) {

    val vm = koinViewModel<HoldingManagementViewModel>()
    val state by vm.state.collectAsState()

    LaunchedEffect(holdingId) {
        vm.dispatch(HoldingManagementEvents.ScreenEntered(holdingId = holdingId))
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onComplete()
    }

    HoldingFormView(
        modifier = modifier,
        ui = state,
        onEvent = vm::dispatch
    )
}

@Composable
private fun HoldingFormView(
    modifier: Modifier,
    ui: HoldingManagementUiState,
    onEvent: (HoldingManagementEvents) -> Unit,
) {

    StableExposedDropdown(
        modifier = modifier,
        label = BROKERAGE_FIELD_LABEL,
        displayValue = ui.brokerage?.name.orEmpty(),
        options = ui.brokerages,
        itemLabel = Brokerage::name,
        onItemSelect = { brokerage ->
            onEvent(HoldingManagementEvents.BrokerageChanged(brokerage))
            onEvent(HoldingManagementEvents.Save)
        },
        error = ui.brokerageError,
        required = true
    )
}

private class HoldingFormPreviewProvider : PreviewParameterProvider<HoldingManagementUiState> {
    override val values: Sequence<HoldingManagementUiState> = sequenceOf(
        HoldingManagementUiState(),
    )
}

@Preview
@Composable
private fun HoldingFormViewPreview(
    @PreviewParameter(HoldingFormPreviewProvider::class) ui: HoldingManagementUiState,
) {
    MaterialTheme {

        Surface {

            Card(modifier = Modifier.padding(16.dp)) {

                HoldingFormView(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    ui = ui,
                    onEvent = {}
                )
            }
        }
    }
}
