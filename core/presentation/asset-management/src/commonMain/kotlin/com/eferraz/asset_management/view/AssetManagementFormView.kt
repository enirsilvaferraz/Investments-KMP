package com.eferraz.asset_management.view

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
import com.eferraz.asset_management.vm.VMEvents
import com.eferraz.asset_management.vm.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetManagementFormView(
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {

        Column(modifier = modifier.widthIn(min = 280.dp, max = 560.dp)) {

            TopAppBar(
                modifier = Modifier.padding(start = 6.dp, bottom = 16.dp),
                title = { Text("Novo investimento") }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                assetManagementForm(ui, ui.issuers, ui.brokerages, onEvent)
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, end = 24.dp, bottom = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
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
