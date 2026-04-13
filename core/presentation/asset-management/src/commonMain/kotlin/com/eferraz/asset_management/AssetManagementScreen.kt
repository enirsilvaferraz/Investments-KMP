package com.eferraz.asset_management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.eferraz.design_system.core.StableMap
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
            viewModel.dispatch(AssetManagementViewModel.Intent.NavigationConsumed)
        }
    }

    AssetManagementFormView(
        issuers = state.issuers,
        draft = state.draft,
        fieldErrors = StableMap(state.fieldErrors),
        saveError = state.saveError,
        isSaving = state.isSaving,
        showDiscardDialog = state.showDiscardDialog,
        onDraftChange = { viewModel.dispatch(AssetManagementViewModel.Intent.DraftChanged(it)) },
        onCategoryChange = { viewModel.dispatch(AssetManagementViewModel.Intent.CategoryChanged(it)) },
        onSave = { viewModel.dispatch(AssetManagementViewModel.Intent.Save) },
        onDismissRequest = { viewModel.dispatch(AssetManagementViewModel.Intent.RequestDismiss) },
        onConfirmDiscard = { viewModel.dispatch(AssetManagementViewModel.Intent.ConfirmDiscard) },
        onCancelDiscard = { viewModel.dispatch(AssetManagementViewModel.Intent.CancelDiscard) },
    )
}