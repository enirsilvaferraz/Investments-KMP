package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Célula individual da tabela
 */
@Composable
internal fun <T> RowScope.TableCell(
    item: T,
    column: TableColumn<T>,
) {
    Box(
        modifier = Modifier.weight(column.weight),
        contentAlignment = when (column.alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.CenterHorizontally -> Alignment.Center
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        column.cellContent.invoke(this@TableCell, item)
    }
}
