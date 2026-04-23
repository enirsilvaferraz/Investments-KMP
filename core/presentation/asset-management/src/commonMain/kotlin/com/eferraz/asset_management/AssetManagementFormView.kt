package com.eferraz.asset_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.scaffolds.AppConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetManagementFormView(
    ui: UiState,
    onEvent: (AssetManagementEvent) -> Unit,
    modifier: Modifier = Modifier,
) {

    if (ui.showDiscardDialog) {
        AppConfirmDialog(
            title = "Descartar alterações?",
            description = "As alterações não guardadas serão perdidas.",
            confirmText = "Descartar",
            onConfirm = { onEvent(AssetManagementEvent.ConfirmDiscard) },
            dismissText = "Continuar a editar",
            onDismiss = { onEvent(AssetManagementEvent.CancelDiscard) },
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {

        Column(modifier = modifier.widthIn(min = 280.dp, max = 560.dp)) {

            TopAppBar(
                modifier = Modifier.padding(start = 6.dp),
                title = { Text("Novo investimento") }
            )

            ui.saveError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                baseForm(ui, ui.issuers, ui.brokerages, onEvent)
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, end = 24.dp, bottom = 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {

                TextButton(
                    onClick = { onEvent(AssetManagementEvent.RequestDismiss) },
                    enabled = !ui.isSaving
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = { onEvent(AssetManagementEvent.Save) },
                    enabled = !ui.isSaving,
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}
