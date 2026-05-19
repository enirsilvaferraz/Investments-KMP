package com.eferraz.asset_management.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.asset_management.helpers.FormTextField

@Composable
internal fun NewTransactionsTable() {

    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = "Transações",
        style = MaterialTheme.typography.headlineSmall
    )

    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Text(
                modifier = Modifier.width(125.dp),
                text = "Data",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.width(130.dp),
                text = "Transação",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.weight(.5f),
                text = "Qtde",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.weight(1.1f),
                text = "Valor Unit.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.weight(1.1f),
                text = "Valor Total",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "         ",
            )
        }

        repeat(times = 10) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                FormTextField(
                    modifier = Modifier.width(125.dp),
                    label = "",
                    value = "2026.00.00",
                    onValueChange = { },
                    errorMessage = null,
                )

                FormTextField(
                    modifier = Modifier.width(130.dp),
                    label = "",
                    value = if (it % 2 == 0) "Compra" else "Venda",
                    onValueChange = { },
                    errorMessage = null,
                )

                FormTextField(
                    modifier = Modifier.weight(.5f),
                    label = "",
                    value = "1000",
                    onValueChange = { },
                    errorMessage = null,
                )

                FormTextField(
                    modifier = Modifier.weight(1.1f),
                    label = "",
                    value = "R$ 1.000,00",
                    onValueChange = { },
                    errorMessage = null,
                )

                FormTextField(
                    modifier = Modifier.weight(1.1f),
                    label = "",
                    value = "R$ 100.000,00",
                    onValueChange = { },
                    errorMessage = null,
                )

                IconButton({}) {
                    Icon(Icons.Default.Close, "", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}