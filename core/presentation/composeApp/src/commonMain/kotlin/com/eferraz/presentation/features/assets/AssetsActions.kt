package com.eferraz.presentation.features.assets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import com.eferraz.presentation.features.assetForm.AssetFormIntent
import com.eferraz.presentation.features.assetForm.AssetFormViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun AssetsActions(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Nothing>,
    formVm: AssetFormViewModel,
) {
    FilledIconButton(
        onClick = {
            scope.launch {
                if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) {
                    navigator.navigateBack()
                } else {
                    // Limpar formulário para modo de cadastro
                    formVm.processIntent(AssetFormIntent.ClearForm)
                    navigator.navigateTo(ThreePaneScaffoldRole.Tertiary)
                }
            }
        },
        colors = if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary) 
            IconButtonDefaults.filledTonalIconButtonColors() 
        else 
            IconButtonDefaults.filledIconButtonColors()
    ) {
        if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Tertiary)
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        else
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

