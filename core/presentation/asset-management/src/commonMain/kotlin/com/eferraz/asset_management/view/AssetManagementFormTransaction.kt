package com.eferraz.asset_management.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.vm.UiState
import com.eferraz.asset_management.vm.VMEvents

@Composable
internal fun TransactionFormContent(
    modifier: Modifier = Modifier,
    ui: UiState,
    onEvent: (VMEvents) -> Unit,
) {

    Column(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        BrokerageField(ui = ui, onEvent = onEvent)

        Text(
            text = "Transações da holding",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (ui.transactions.isEmpty()) {

            Text(
                text = "Histórico disponível após salvar a holding",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                ui.transactions.forEach { transaction ->

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "${transaction.type} - ${transaction.date}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                text = "Valor total: ${transaction.totalValue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!transaction.observations.isNullOrBlank()) {
                                Text(
                                    text = transaction.observations.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}