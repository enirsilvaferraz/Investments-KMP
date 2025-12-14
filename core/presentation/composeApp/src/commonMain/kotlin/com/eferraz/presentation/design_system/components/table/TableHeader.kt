package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Cabeçalho da tabela
 */
@Composable
internal fun <T> TableHeader(
    columns: List<TableColumn<T>>,
    sortState: TableSortState<T>,
    theme: TableTheme,
    onSort: (Int, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.headerBackground)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columns.forEachIndexed { index, column ->
            val isSorted = sortState.columnIndex == index

            Box(
                modifier = Modifier
                    .weight(column.weight)
                    .then(
                        if (column.isSortable) {
                            Modifier.clickable {
                                onSort(index, if (isSorted) !sortState.ascending else true)
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = when (column.alignment) {
                    Alignment.Start -> Alignment.CenterStart
                    Alignment.CenterHorizontally -> Alignment.Center
                    Alignment.End -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = theme.headerText
                    )

                    if (isSorted && column.isSortable) {
                        Icon(
                            imageVector = if (sortState.ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = theme.sortIcon
                        )
                    }
                }
            }
        }
    }
}
