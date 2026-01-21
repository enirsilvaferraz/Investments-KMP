package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp

// ============================================================================
// CONSTANTES E HELPERS
// ============================================================================

private val HEADER_HEIGHT = 54.dp
private val CELL_HEIGHT = 45.dp
private val FOOTER_HEIGHT = 70.dp
private val CELL_PADDING = 8.dp
private val FOOTER_PADDING = 16.dp

@Composable
private fun headerFooterTextStyle() = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)

private fun Alignment.Horizontal.toBoxAlignment(): Alignment {
    return when (this) {
        Alignment.Start -> Alignment.CenterStart
        Alignment.CenterHorizontally -> Alignment.Center
        Alignment.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
}

// ============================================================================
// INTERFACE DE RENDERIZAÇÃO DE CÉLULAS
// ============================================================================

internal interface CellRenderer<T> {

    @Composable
    fun renderHeader(
        column: ColumnData<T>,
        index: Int,
        sortState: SortState,
        onSort: (Int) -> Unit,
    )

    @Composable
    fun renderCell(
        column: ColumnData<T>,
        item: T,
    )

    @Composable
    fun renderFooter(
        column: ColumnData<T>,
        data: List<T>,
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
        onSort: (Int) -> Unit,
    ) {
        val textAlign = when (column.alignment) {
            Alignment.Start -> androidx.compose.ui.text.style.TextAlign.Start
            Alignment.CenterHorizontally -> androidx.compose.ui.text.style.TextAlign.Center
            Alignment.End -> androidx.compose.ui.text.style.TextAlign.End
            else -> androidx.compose.ui.text.style.TextAlign.Start
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT).padding(CELL_PADDING),
            contentAlignment = column.alignment.toBoxAlignment()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (column.alignment) {
                    Alignment.Start -> Arrangement.Start
                    Alignment.CenterHorizontally -> Arrangement.Center
                    Alignment.End -> Arrangement.End
                    else -> Arrangement.Start
                }
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = CELL_PADDING),
                    text = column.header,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = headerFooterTextStyle(),
                    textAlign = textAlign
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
    }

    @Composable
    override fun renderCell(column: ColumnData<T>, item: T) {
        Box(
            modifier = Modifier.fillMaxWidth().height(CELL_HEIGHT).padding(CELL_PADDING),
            contentAlignment = column.alignment.toBoxAlignment()
        ) {
            column.cell.invoke(this, item)
        }
    }

    @Composable
    override fun renderFooter(column: ColumnData<T>, data: List<T>) {

        if (column.footer == null) return

        val textAlign = when (column.alignment) {
            Alignment.Start -> androidx.compose.ui.text.style.TextAlign.Start
            Alignment.CenterHorizontally -> androidx.compose.ui.text.style.TextAlign.Center
            Alignment.End -> androidx.compose.ui.text.style.TextAlign.End
            else -> androidx.compose.ui.text.style.TextAlign.Start
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(FOOTER_HEIGHT).padding(FOOTER_PADDING),
            contentAlignment = column.alignment.toBoxAlignment()
        ) {
            Text(
                text = column.footer(data),
                color = MaterialTheme.colorScheme.onSurface,
                style = headerFooterTextStyle(),
                textAlign = textAlign
            )
        }
    }
}

