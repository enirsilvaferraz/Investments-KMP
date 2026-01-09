package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.theme.AppTheme

// ============================================================================
// INTERFACES (DIP - Dependency Inversion Principle)
// ============================================================================

private interface SortStrategy<T> {
    fun createComparator(
        sortedColumnIndex: Int,
        isAscending: Boolean,
        columns: List<((T) -> Comparable<*>)?>
    ): Comparator<T>
}

private interface CellRenderer<T> {
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

private interface ResponsiveLayoutStrategy {
    fun getWeight(index: Int): Float?
    fun calculateCellWidth(index: Int, availableWidth: Int?): Int?
    fun updateWidth(index: Int, width: Int)
}

// ============================================================================
// API PÚBLICA
// ============================================================================

public interface UiTableContentScope<T> {
    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: (List<T>) -> String = { "" },
    )

    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        cellValue: @Composable (T) -> String,
        footer: (List<T>) -> String = { "" },
    )
}

@Composable
internal fun <T> UiTable(
    modifier: Modifier = Modifier,
    data: List<T>,
    contentPadding: PaddingValues = PaddingValues(),
    onSelect: ((T) -> Unit)? = null,
    content: UiTableContentScope<T>.() -> Unit,
) {
    // PERFORMANCE: Cria scope apenas uma vez usando remember com chave estável
    val scope = remember {
        UiTableScope<T>().apply(content)
    }

    // PERFORMANCE: Usa headers como chave estável para remember
    val columns = remember(scope.columns.map { it.header }) { scope.columns }
    val sortedByFunctions = remember(columns) { columns.map { it.sortedBy } }

    // PERFORMANCE: Estratégias criadas uma vez e lembradas
    val sortStrategy = remember { DefaultSortStrategy<T>() }
    val cellRenderer = remember { DefaultCellRenderer<T>() }

    val sortState = rememberSortState(data, sortedByFunctions, sortStrategy)
    val responsiveState = rememberResponsiveState(columns.size)

    // PERFORMANCE: Lembra callbacks para evitar recriação de lambdas
    val onSort = remember { { index: Int -> sortState.sort(index) } }
    val onSelectUpdated = rememberUpdatedState(onSelect)
    val totalItems = remember(sortState.sortedData.size) { sortState.sortedData.size }

    Column {
        LazyColumn(
            modifier = modifier.fillMaxWidth().weight(1f),
            contentPadding = contentPadding
        ) {
            stickyHeader {
                TableHeader(
                    columns = columns,
                    sortState = sortState,
                    responsiveState = responsiveState,
                    cellRenderer = cellRenderer,
                    onSort = onSort
                )
            }

            itemsIndexed(
                items = sortState.sortedData,
                // PERFORMANCE: Usa item como key quando possível (requer T ter equals/hashCode)
                // Por enquanto usa index, mas idealmente deveria usar item.hashCode() ou item.toString()
                key = { index, item -> index }
            ) { index, item ->
                TableRow(
                    item = item,
                    index = index,
                    totalItems = totalItems,
                    columns = columns,
                    responsiveState = responsiveState,
                    cellRenderer = cellRenderer,
                    onSelect = onSelectUpdated
                )
            }
        }

        TableFooter(
            data = data,
            columns = columns,
            responsiveState = responsiveState,
            cellRenderer = cellRenderer
        )
    }
}

// ============================================================================
// IMPLEMENTAÇÃO INTERNA
// ============================================================================

@Stable
private class UiTableScope<T> : UiTableContentScope<T> {
    val columns = mutableListOf<ColumnData<T>>()

    override fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)?,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: (List<T>) -> String,
    ) {
        columns.add(ColumnData(header, sortedBy, cellContent, footer))
    }

    override fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)?,
        cellValue: @Composable (T) -> String,
        footer: (List<T>) -> String,
    ) {
        columns.add(
            ColumnData(
                header = header,
                sortedBy = sortedBy,
                cell = @Composable { current ->
                    Text(
                        text = cellValue(current),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                footer = footer
            )
        )
    }
}

@Stable
private data class ColumnData<T>(
    val header: String,
    val sortedBy: ((T) -> Comparable<*>)?,
    val cell: @Composable BoxScope.(T) -> Unit,
    val footer: (List<T>) -> String,
) {
    fun isSortable() = sortedBy != null
}

// ============================================================================
// IMPLEMENTAÇÕES DE ESTRATÉGIAS (DIP)
// ============================================================================

@Stable
private class DefaultSortStrategy<T> : SortStrategy<T> {
    override fun createComparator(
        sortedColumnIndex: Int,
        isAscending: Boolean,
        columns: List<((T) -> Comparable<*>)?>
    ): Comparator<T> {
        val comparator = compareBy<T> { columns[sortedColumnIndex]?.invoke(it) }
        return if (isAscending) comparator else comparator.reversed()
    }
}

@Stable
private class DefaultCellRenderer<T> : CellRenderer<T> {
    @Composable
    override fun renderHeader(
        column: ColumnData<T>,
        index: Int,
        sortState: SortState,
        onSort: (Int) -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = column.header,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
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
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            column.cell.invoke(this, item)
        }
    }

    @Composable
    override fun renderFooter(column: ColumnData<T>, data: List<T>) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = column.footer(data),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

// ============================================================================
// ESTADO DE ORDENAÇÃO
// ============================================================================

@Stable
private data class SortState(
    val sortedColumnIndex: Int = 0,
    val isAscending: Boolean = true,
) {
    fun sort(index: Int): SortState {
        val isSameColumn = sortedColumnIndex == index
        return SortState(
            sortedColumnIndex = index,
            isAscending = if (isSameColumn) !isAscending else true
        )
    }
}

@Stable
private class SortStateManager<T> {
    var sortState: SortState by mutableStateOf(SortState())
        private set
    val sortedData: SnapshotStateList<T> = mutableStateListOf()

    fun sort(index: Int) {
        sortState = sortState.sort(index)
    }
}

@Stable
private class SortDataCalculator<T>(
    private val strategy: SortStrategy<T>
) {
    fun calculate(
        data: List<T>,
        sortState: SortState,
        columns: List<((T) -> Comparable<*>)?>
    ): List<T> {
        val comparator = strategy.createComparator(
            sortState.sortedColumnIndex,
            sortState.isAscending,
            columns
        )
        return data.sortedWith(comparator)
    }
}

@Composable
private fun <T> rememberSortState(
    data: List<T>,
    columns: List<((T) -> Comparable<*>)?>,
    strategy: SortStrategy<T>
): SortStateManager<T> {
    val manager = remember { SortStateManager<T>() }
    val calculator = remember(strategy) { SortDataCalculator(strategy) }

    // PERFORMANCE: derivedStateOf observa mudanças automaticamente em manager.sortState, data e columns
    val sortedData = remember(manager.sortState, data, columns) {
        derivedStateOf {
            calculator.calculate(data, manager.sortState, columns)
        }
    }

    // PERFORMANCE: LaunchedEffect observa apenas o valor calculado
    LaunchedEffect(sortedData.value) {
        manager.sortedData.clear()
        manager.sortedData.addAll(sortedData.value)
    }

    return manager
}

// ============================================================================
// ESTADO RESPONSIVO
// ============================================================================

@Stable
private class ResponsiveState(
    val columnCount: Int
) : ResponsiveLayoutStrategy {
    val columnWidths: SnapshotStateMap<Int, Int> = mutableStateMapOf()

    override fun getWeight(index: Int): Float? {
        if (columnWidths.size != columnCount || columnCount == 0) return null
        val totalWidth = columnWidths.values.sum()
        if (totalWidth <= 0) return null
        return (columnWidths[index] ?: return null).toFloat() / totalWidth
    }

    override fun calculateCellWidth(index: Int, availableWidth: Int?): Int? {
        return availableWidth?.let { (it * (getWeight(index) ?: return null)).toInt() }
    }

    override fun updateWidth(index: Int, width: Int) {
        val currentWidth = columnWidths[index] ?: 0
        if (width > currentWidth) {
            columnWidths[index] = width
        }
    }
}

@Composable
private fun rememberResponsiveState(columnCount: Int): ResponsiveState {
    return remember(columnCount) { ResponsiveState(columnCount) }
}

// ============================================================================
// COMPONENTES DE LAYOUT
// ============================================================================

@Composable
private fun <T> TableHeader(
    columns: List<ColumnData<T>>,
    sortState: SortStateManager<T>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
    onSort: (Int) -> Unit,
) {
    ResponsiveRow(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        state = responsiveState
    ) { availableWidth ->
        columns.forEachIndexed { index, col ->
            // PERFORMANCE: Lembra lambda de click para cada coluna
            val onColumnClick = remember(index, onSort) { { onSort(index) } }

            Cell(
                state = responsiveState,
                index = index,
                availableWidth = availableWidth,
                modifier = Modifier
                    .height(54.dp)
                    .clickable(enabled = col.isSortable(), onClick = onColumnClick)
                    .padding(8.dp)
            ) {
                cellRenderer.renderHeader(col, index, sortState.sortState, onSort)
            }
        }
    }
}

@Composable
private fun <T> TableRow(
    item: T,
    index: Int,
    totalItems: Int,
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
    onSelect: androidx.compose.runtime.State<((T) -> Unit)?>,
) {
    // PERFORMANCE: Lembra lambda de click apenas uma vez por item
    val onRowClick = remember(item, onSelect) {
        {
            val callback = onSelect.value
            if (callback != null) callback(item)
        }
    }

    ResponsiveRow(
        modifier = Modifier.clickable(onSelect.value != null, onClick = onRowClick),
        state = responsiveState,
        showDivider = index < totalItems - 1
    ) { availableWidth ->
        columns.forEachIndexed { colIndex, col ->
            Cell(
                state = responsiveState,
                index = colIndex,
                availableWidth = availableWidth,
                modifier = Modifier.height(54.dp).padding(8.dp)
            ) {
                cellRenderer.renderCell(col, item)
            }
        }
    }
}

@Composable
private fun <T> TableFooter(
    data: List<T>,
    columns: List<ColumnData<T>>,
    responsiveState: ResponsiveState,
    cellRenderer: CellRenderer<T>,
) {
    ResponsiveRow(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        state = responsiveState
    ) { availableWidth ->
        columns.forEachIndexed { index, col ->
            Cell(
                state = responsiveState,
                index = index,
                availableWidth = availableWidth,
                modifier = Modifier.height(54.dp).padding(8.dp)
            ) {
                cellRenderer.renderFooter(col, data)
            }
        }
    }
}

@Composable
private fun ResponsiveRow(
    state: ResponsiveState,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    content: @Composable RowScope.(availableWidth: Int?) -> Unit,
) {
    var rowWidth by remember { mutableStateOf<Int?>(null) }
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { rowWidth = it.size.width },
            verticalAlignment = Alignment.CenterVertically
        ) {
            content(rowWidth)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun Cell(
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

// ============================================================================
// PREVIEW
// ============================================================================

private data class UITableRowData(val text1: String, val text2: String, val text3: String)

@Preview(widthDp = 500)
@Composable
internal fun UITablePreview() {
    AppTheme {
        Surface(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp)) {
            UiTable(
                data = listOf(
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2 Qqwek nnwu", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2 dc", "Texto 3B"),
                ),
                onSelect = { println("Selected: $it") }
            ) {
                column(header = "Header 1", sortedBy = { it.text1 }, cellValue = { it.text1 })
                column(header = "Header 2", sortedBy = { it.text2 }, cellValue = { it.text2 }, footer = { "Footer teste" })
                column(header = "Header 3", sortedBy = { it.text3 }, cellValue = { it.text3 })
                column(header = "Header 4", cellValue = { it.text1 }, footer = { it.size.toString() })
            }
        }
    }
}
