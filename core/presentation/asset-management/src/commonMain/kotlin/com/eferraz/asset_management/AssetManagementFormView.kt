package com.eferraz.asset_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.scaffolds.AppConfirmDialog
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetManagementFormView(
    issuers: List<Issuer>,
    brokerages: List<Brokerage>,
    draft: AssetDraft,
    saveError: String?,
    isSaving: Boolean,
    showDiscardDialog: Boolean,
    onEvent: (AssetManagementEvent) -> Unit,
    modifier: Modifier = Modifier,
) {

    if (showDiscardDialog) {
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

            saveError?.let { msg ->
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
                baseForm(draft, issuers, brokerages, onEvent)
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, end = 24.dp, bottom = 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {

                TextButton(
                    onClick = { onEvent(AssetManagementEvent.RequestDismiss) },
                    enabled = !isSaving
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = { onEvent(AssetManagementEvent.Save) },
                    enabled = !isSaving,
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

/**
 * Pré-visualização do formulário (Android Studio), sem Koin nem ViewModel.
 * O estado é local só para inspecionar o layout; o fluxo real continua a usar o ViewModel.
 */
@Preview//(showBackground = true, widthDp = 420, heightDp = 900, name = "AssetManagementScreen — RF")
@Composable
internal fun AssetManagementScreenPreview() {
    AssetManagementFormPreviewContent()
}

@Composable
private fun AssetManagementFormPreviewContent() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var draft by remember { mutableStateOf(initialAssetDraft()) }
            val onEvent: (AssetManagementEvent) -> Unit = { e ->
                when (e) {
                    is AssetManagementEvent.ConfirmDiscard -> { draft = initialAssetDraft() }
                    else -> draft.applyFormEvent(e)?.let { draft = it }
                }
            }
            AssetManagementFormView(
                issuers = listOf(
                    Issuer(id = 1L, name = "Banco Preview"),
                    Issuer(id = 2L, name = "Tesouro / Selic"),
                ),
                brokerages = listOf(
                    Brokerage(id = 1L, name = "Corretora A"),
                    Brokerage(id = 2L, name = "Corretora B"),
                ),
                draft = draft,
                saveError = null,
                isSaving = false,
                showDiscardDialog = false,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview//(showBackground = true, widthDp = 420, heightDp = 900, name = "Formulário — renda variável")
@Composable
internal fun AssetManagementFormVariableIncomePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var draft by remember {
                mutableStateOf(
                    initialAssetDraft().copy(
                        category = InvestmentCategory.VARIABLE_INCOME,
                        issuer = Issuer(id = 1L, name = "Banco Preview"),
                        brokerage = Brokerage(id = 1L, name = "Corretora Preview"),
                        variableName = "Ação exemplo",
                    ),
                )
            }
            val onEvent: (AssetManagementEvent) -> Unit = { e ->
                when (e) {
                    is AssetManagementEvent.ConfirmDiscard -> { draft = initialAssetDraft() }
                    else -> draft.applyFormEvent(e)?.let { draft = it }
                }
            }
            AssetManagementFormView(
                issuers = listOf(Issuer(id = 1L, name = "Banco Preview")),
                brokerages = listOf(Brokerage(id = 1L, name = "Corretora Preview")),
                draft = draft,
                saveError = null,
                isSaving = false,
                showDiscardDialog = false,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
