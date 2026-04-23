package com.eferraz.asset_management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun AssetManagementScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: AssetManagementViewModel = koinViewModel()
    val ui by viewModel.state.collectAsState()

    val onDismissUpdated = rememberUpdatedState(onDismiss)
    LaunchedEffect(ui.navigateAway) {
        if (ui.navigateAway) {
            onDismissUpdated.value()
            viewModel.dispatch(AssetManagementEvent.NavigationConsumed)
        }
    }

    AssetManagementFormView(
        ui = ui,
        onEvent = viewModel::dispatch,
        modifier = modifier,
    )
}
