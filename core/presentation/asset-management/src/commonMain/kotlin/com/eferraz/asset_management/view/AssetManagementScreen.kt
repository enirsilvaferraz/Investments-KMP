package com.eferraz.asset_management.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.asset_management.vm.AssetManagementViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun AssetManagementScreen(
    holdingId: Long?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: AssetManagementViewModel = koinViewModel()
    val ui by viewModel.state.collectAsState()

    LaunchedEffect(holdingId) {
        viewModel.dispatch(VMEvents.ScreenEntered(holdingId = holdingId))
    }

    val onDismissUpdated = rememberUpdatedState(onDismiss)
    LaunchedEffect(ui.navigateAway) {
        if (ui.navigateAway) {
            onDismissUpdated.value()
            viewModel.dispatch(VMEvents.NavigationConsumed)
        }
    }

    AssetManagementFormView(
        ui = ui,
        onEvent = viewModel::dispatch,
        modifier = Modifier.wrapContentHeight(),
    )
}
