package com.eferraz.asset_management.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.asset_management.vm.AssetManagementViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun AssetManagementScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: AssetManagementViewModel = koinViewModel()
    val ui by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(VMEvents.ScreenEntered)
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
        modifier = modifier,
    )
}
