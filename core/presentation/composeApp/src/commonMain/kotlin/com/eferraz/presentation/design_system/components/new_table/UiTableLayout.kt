package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// ============================================================================
// COMPONENTES DE LAYOUT - HEADER, ROW, FOOTER
// ============================================================================

@Composable
private fun headerFooterModifier() = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)

@Composable
internal fun <T> TableHeader(
    columns: List<ColumnData<T>>,
    sortState: SortStateManager<T>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
    onSort: (Int) -> Unit,
) {
    ResponsiveRow(
        modifier = headerFooterModifier(),
        state = responsiveState
    ) { availableWidth ->
        renderCells(
            columns = columns,
            responsiveState = responsiveState,
            availableWidth = availableWidth
        ) { index, col ->
            val onColumnClick = remember(index, onSort) { { onSort(index) } }
            Cell(
                state = responsiveState,
                index = index,
                availableWidth = availableWidth,
                modifier = Modifier.clickable(enabled = col.isSortable(), onClick = onColumnClick)
            ) {
                cellRenderer.renderHeader(col, index, sortState.sortState, onSort)
            }
        }
    }
}

@Composable
internal fun <T> TableRow(
    item: T,
    index: Int,
    totalItems: Int,
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
    onSelect: androidx.compose.runtime.State<((T) -> Unit)?>,
) {
    val onRowClick = remember(item, onSelect) {
        {
            val callback = onSelect.value
            if (callback != null) callback(item)
        }
    }

    ResponsiveRow(
        modifier = Modifier.clickable(onSelect.value != null, onClick = onRowClick),
        state = responsiveState,
    ) { availableWidth ->
        renderCells(
            columns = columns,
            responsiveState = responsiveState,
            availableWidth = availableWidth
        ) { colIndex, col ->
            Cell(
                state = responsiveState,
                index = colIndex,
                availableWidth = availableWidth
            ) {
                cellRenderer.renderCell(col, item)
            }
        }
    }
}

@Composable
internal fun <T> TableFooter(
    data: List<T>,
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
) {
    ResponsiveRow(
        modifier = headerFooterModifier(),
        state = responsiveState
    ) { availableWidth ->
        renderCells(
            columns = columns,
            responsiveState = responsiveState,
            availableWidth = availableWidth
        ) { index, col ->
            Cell(
                state = responsiveState,
                index = index,
                availableWidth = availableWidth
            ) {
                cellRenderer.renderFooter(col, data)
            }
        }
    }
}

@Composable
private fun <T> renderCells(
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    availableWidth: Int?,
    cellContent: @Composable (index: Int, column: ColumnData<T>) -> Unit
) {
    columns.forEachIndexed { index, col ->
        cellContent(index, col)
    }
}

// ============================================================================
// COMPONENTES DE LAYOUT - ROW E CELULA RESPONSIVA
// ============================================================================

@Composable
internal fun ResponsiveRow(
    state: ResponsiveState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.(availableWidth: Int?) -> Unit,
) {
    var rowWidth by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                rowWidth = coordinates.size.width
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content(rowWidth)
    }
}

@Composable
internal fun Cell(
    state: ResponsiveState,
    index: Int,
    availableWidth: Int?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    val density = LocalDensity.current
    val calculatedWidth = state.calculateCellWidth(index, availableWidth)

    // PERFORMANCE: Lembra modifier apenas quando necessário
    val widthModifier = remember(calculatedWidth, density) {
        if (calculatedWidth != null) {
            Modifier.width(with(density) { calculatedWidth.toDp() })
        } else {
            Modifier.width(IntrinsicSize.Max)
        }
    }

    Box(
        modifier = widthModifier
            .then(modifier)
            .onGloballyPositioned { coordinates ->
                state.updateWidth(index, coordinates.size.width)
            }
    ) {
        content()
    }
}

