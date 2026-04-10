package com.eferraz.asset_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Ponto de entrada público do módulo: diálogo de cadastro de investimento.
 *
 * @param onDismiss Chamado quando o fluxo deve fechar o diálogo (sucesso, cancelamento ou descarte).
 */
@Composable
public fun AssetManagementScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssetManagementScreenContent(
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
