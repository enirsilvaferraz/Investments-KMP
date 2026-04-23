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
    val state by viewModel.state.collectAsState()

    val onDismissUpdated = rememberUpdatedState(onDismiss)
    LaunchedEffect(state.navigateAway) {
        if (state.navigateAway) {
            onDismissUpdated.value()
            viewModel.dispatch(AssetManagementEvent.NavigationConsumed)
        }
    }

    AssetManagementFormView(
        issuers = state.issuers,
        brokerages = state.brokerages,
        draft = state.draft,
        saveError = state.saveError,
        isSaving = state.isSaving,
        showDiscardDialog = state.showDiscardDialog,
        onEvent = viewModel::dispatch,
    )
}
