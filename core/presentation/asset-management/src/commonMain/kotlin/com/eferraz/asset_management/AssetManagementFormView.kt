package com.eferraz.asset_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.window.DialogProperties
import com.eferraz.design_system.core.StableMap
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetManagementFormView(
    issuers: List<Issuer>,
    brokerages: List<Brokerage>,
    draft: AssetDraft,
    fieldErrors: StableMap<String, String>,
    saveError: String?,
    isSaving: Boolean,
    showDiscardDialog: Boolean,
    onDraftChange: (AssetDraft) -> Unit,
    onCategoryChange: (InvestmentCategory) -> Unit,
    onSave: () -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onCancelDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {

    if (showDiscardDialog) {
        AssetManagementDiscardDialog(
            onConfirmDiscard = onConfirmDiscard,
            onCancelDiscard = onCancelDiscard,
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {

        Column {

            TopAppBar(
                title = { Text("Novo investimento") }
            )

            Column(
                modifier = modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(min = 280.dp, max = 560.dp)
                    .wrapContentSize(align = Alignment.Center),
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                AssetManagementFormSaveErrorBanner(saveError = saveError)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    baseForm(draft, onCategoryChange, issuers, brokerages, onDraftChange, fieldErrors)
                }

                AssetManagementFormActionRow(
                    onDismissRequest = onDismissRequest,
                    onSave = onSave,
                    isSaving = isSaving,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun AssetManagementDiscardDialog(
    onConfirmDiscard: () -> Unit,
    onCancelDiscard: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onCancelDiscard,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Descartar alterações?") },
        text = { Text("As alterações não guardadas serão perdidas.") },
        confirmButton = {
            TextButton(onClick = onConfirmDiscard) {
                Text("Descartar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelDiscard) {
                Text("Continuar a editar")
            }
        },
    )
}

@Composable
private fun AssetManagementFormSaveErrorBanner(saveError: String?) {
    Column {
        if (saveError != null) {
            Text(
                text = saveError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AssetManagementFormActionRow(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {

        TextButton(
            onClick = onDismissRequest,
            enabled = !isSaving
        ) {
            Text("Cancelar")
        }

        Button(
            onClick = onSave,
            enabled = !isSaving,
        ) {
            Text("Salvar")
        }
    }
}

/**
 * Pré-visualização do formulário (Android Studio), sem Koin nem ViewModel.
 * O estado é local só para inspecionar o layout; o fluxo real continua a usar o ViewModel.
 */
@Preview(showBackground = true, widthDp = 420, heightDp = 900, name = "AssetManagementScreen — RF")
@Composable
internal fun AssetManagementScreenPreview() {
    AssetManagementFormPreviewContent()
}

@Composable
private fun AssetManagementFormPreviewContent() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var draft by remember { mutableStateOf(initialAssetDraft()) }
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
                fieldErrors = StableMap(emptyMap()),
                saveError = null,
                isSaving = false,
                showDiscardDialog = false,
                onDraftChange = { draft = it },
                onCategoryChange = { draft = draft.withCategoryPreservingIssuerAndObs(it) },
                onSave = {},
                onDismissRequest = {},
                onConfirmDiscard = {},
                onCancelDiscard = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900, name = "Formulário — renda variável")
@Composable
internal fun AssetManagementFormVariableIncomePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var draft by remember {
                mutableStateOf(
                    initialAssetDraft().copy(
                        category = InvestmentCategory.VARIABLE_INCOME,
                        issuerId = 1L,
                        variableName = "Ação exemplo",
                    ),
                )
            }
            AssetManagementFormView(
                issuers = listOf(Issuer(id = 1L, name = "Banco Preview")),
                brokerages = listOf(Brokerage(id = 1L, name = "Corretora Preview")),
                draft = draft,
                fieldErrors = StableMap(emptyMap()),
                saveError = null,
                isSaving = false,
                showDiscardDialog = false,
                onDraftChange = { draft = it },
                onCategoryChange = { draft = draft.withCategoryPreservingIssuerAndObs(it) },
                onSave = {},
                onDismissRequest = {},
                onConfirmDiscard = {},
                onCancelDiscard = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
