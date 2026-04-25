package com.eferraz.asset_management.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetManagementFormView(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = Modifier.widthIn(min = 840.dp, max = 1120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {

        TopAppBar(
            modifier = Modifier.padding(start = 6.dp),
            title = { Text("Novo investimento") }
        )

        Column(
            modifier = modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {

                AssetManagementFormContent(
                    modifier = Modifier.weight(1f),
                    ui = ui,
                    issuers = ui.issuers,
                    onEvent = onEvent,
                )

                TransactionFormContent(
                    modifier = Modifier.weight(1f),
                    ui = ui,
                    onEvent = onEvent
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.Bottom,
            ) {

                TextButton(
                    onClick = { onEvent(VMEvents.RequestDismiss) },
                    enabled = !ui.isSaving
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = { onEvent(VMEvents.Save) },
                    enabled = !ui.isSaving,
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}
