package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Célula individual da tabela
 */
@Composable
internal fun <T> RowScope.TableCell(
    item: T,
    column: TableColumn<T>,
) {

    Box(
        modifier = Modifier.weight(column.weight)
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        contentAlignment = alignment(column)
    ) {
        column.cellContent.invoke(this@TableCell, item)
    }
}
