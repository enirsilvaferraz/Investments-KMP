package com.eferraz.asset_management.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.eferraz.asset_management.vm.AssetManagementEvent
import com.eferraz.asset_management.vm.AssetManagementViewModel
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
            viewModel.dispatch(AssetManagementEvent.NavigationConsumed) // TODO Verificar uma maneira de limpar completamente o state
        }
    }

    AssetManagementFormView(
        ui = ui,
        onEvent = viewModel::dispatch,
        modifier = modifier,
    )
}
