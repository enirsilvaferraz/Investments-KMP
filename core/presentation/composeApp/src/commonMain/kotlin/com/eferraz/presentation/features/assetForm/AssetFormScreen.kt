package com.eferraz.presentation.features.assetForm

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun AssetFormRoute() {
    val vm = koinViewModel<AssetFormViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

    // Limpa mensagens após alguns segundos
//    LaunchedEffect(state.successMessage, state.errorMessage) {
//        if (state.successMessage != null || state.errorMessage != null) {
//            kotlinx.coroutines.delay(3000)
//            vm.processIntent(AssetFormIntent.ClearMessages)
//        }
//    }

    AppScaffold(
        title = "Cadastro de Ativos",
        navigator = navigator,
        mainPane = {
            AssetFormContent(
                state = state,
                onIntent = { vm.processIntent(it) },
            )
        },
    )
}

