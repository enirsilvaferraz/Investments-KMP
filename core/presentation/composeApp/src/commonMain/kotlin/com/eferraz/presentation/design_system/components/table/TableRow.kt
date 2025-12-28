package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.utils.thenIf

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
                .height(52.dp)
                .thenIf(onRowClick != null, { Modifier.clickable { onRowClick?.invoke(item) } }),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {

            columns.forEach { column ->
                TableCell(
                    item = item,
                    column = column
                )
            }
        }

        if (showDivider)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = dividerColor,
                thickness = 1.dp
            )
    }
}
