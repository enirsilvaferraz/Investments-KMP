package com.eferraz.design_system.scaffolds

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties

@Composable
public fun AppConfirmDialog(
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
internal fun AppConfirmDialogPreview() {
    MaterialTheme {
        AppConfirmDialog(
            title = "Confirmar ação",
            description = "Esta operação não pode ser desfeita. Deseja continuar?",
            confirmText = "Confirmar",
            onConfirm = {},
            dismissText = "Cancelar",
            onDismiss = {},
        )
    }
}
