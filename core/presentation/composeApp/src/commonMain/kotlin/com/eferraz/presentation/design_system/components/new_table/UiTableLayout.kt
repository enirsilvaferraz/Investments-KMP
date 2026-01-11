package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    ) {
        renderCells(
            columns = columns,
            responsiveState = responsiveState
        ) { index, col ->

            val onColumnClick = remember(index, onSort) { { onSort(index) } }

            Cell(
                state = responsiveState,
                index = index,
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
    onSelect: State<((T) -> Unit)?>,
) {

    val onRowClick = remember(item, onSelect) {
        {
            val callback = onSelect.value
            if (callback != null) callback(item)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val baseColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val hoverColor = MaterialTheme.colorScheme.surfaceContainer
    val rowBackgroundColor = if (isHovered) hoverColor else baseColor

    ResponsiveRow(
        modifier = Modifier
            .background(rowBackgroundColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(onSelect.value != null, onClick = onRowClick),
        state = responsiveState,
    ) {
        renderCells(
            columns = columns,
            responsiveState = responsiveState
        ) { colIndex, col ->

            Cell(
                state = responsiveState,
                index = colIndex
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
    ) {
        renderCells(
            columns = columns,
            responsiveState = responsiveState
        ) { index, col ->

            Cell(
                state = responsiveState,
                index = index
            ) {

                cellRenderer.renderFooter(col, data)
            }
        }
    }
}

@Composable
private fun <T> RowScope.renderCells(
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    cellContent: @Composable RowScope.(index: Int, column: ColumnData<T>) -> Unit,
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
    content: @Composable RowScope.() -> Unit,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        content()
    }
}

@Composable
internal fun RowScope.Cell(
    state: ResponsiveState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    val weight = state.getWeight(index)

    Box(
        modifier = Modifier
            .weight(weight)
//            .background((if (index % 2 == 0) Color.Blue else Color.Red).copy(alpha = 0.4f))
            .then(modifier)
    ) {

        content()
    }
}

