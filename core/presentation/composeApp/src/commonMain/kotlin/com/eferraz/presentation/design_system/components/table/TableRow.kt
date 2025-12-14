package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Linha de dados da tabela
 */
@Composable
internal fun <T> TableRow(
    item: T,
    columns: List<TableColumn<T>>,
    backgroundColor: Color,
    dividerColor: Color,
    onRowClick: ((T) -> Unit)?,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .heightIn(min = 52.dp)
                .then(if (onRowClick != null) Modifier.clickable { onRowClick(item) } else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                TableCell(
                    item = item,
                    column = column
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = dividerColor,
                thickness = 1.dp
            )
        }
    }
}
