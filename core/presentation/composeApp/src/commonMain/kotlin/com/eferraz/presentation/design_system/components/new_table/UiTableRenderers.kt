package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

// ============================================================================
// CONSTANTES E HELPERS
// ============================================================================

private val CELL_HEIGHT = 54.dp
private val CELL_PADDING = 8.dp

@Composable
private fun headerFooterTextStyle() = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)

// ============================================================================
// INTERFACE DE RENDERIZAÇÃO DE CÉLULAS
// ============================================================================

internal interface CellRenderer<T> {

    @Composable
    fun renderHeader(
        column: ColumnData<T>,
        index: Int,
        sortState: SortState,
        onSort: (Int) -> Unit
    )

    @Composable
    fun renderCell(
        column: ColumnData<T>,
        item: T
    )

    @Composable
    fun renderFooter(
        column: ColumnData<T>,
        data: List<T>
    )
}

// ============================================================================
// IMPLEMENTAÇÃO PADRÃO DE RENDERIZAÇÃO
// ============================================================================

@Stable
internal class DefaultCellRenderer<T> : CellRenderer<T> {

    @Composable
    override fun renderHeader(
        column: ColumnData<T>,
        index: Int,
        sortState: SortState,
        onSort: (Int) -> Unit
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().height(CELL_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                modifier = Modifier.padding(CELL_PADDING),
                text = column.header,
                color = MaterialTheme.colorScheme.onSurface,
                style = headerFooterTextStyle(),
            )

            if (sortState.sortedColumnIndex == index) {

                Icon(
                    imageVector = if (sortState.isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    @Composable
    override fun renderCell(column: ColumnData<T>, item: T) {
        CellContentBox {
            column.cell.invoke(this, item)
        }
    }

    @Composable
    override fun renderFooter(column: ColumnData<T>, data: List<T>) {
        CellContentBox {
            Text(
                text = column.footer(data),
                color = MaterialTheme.colorScheme.onSurface,
                style = headerFooterTextStyle(),
            )
        }
    }

    @Composable
    private fun CellContentBox(content: @Composable BoxScope.() -> Unit) {
        Box(
            modifier = Modifier.fillMaxWidth().height(CELL_HEIGHT).padding(CELL_PADDING),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

