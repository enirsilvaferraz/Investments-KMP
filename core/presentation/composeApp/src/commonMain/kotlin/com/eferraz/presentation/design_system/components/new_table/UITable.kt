package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.theme.AppTheme

// ============================================================================
// API PÚBLICA
// ============================================================================

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
                key = { index, item -> item.hashCode() }
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

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
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
// PREVIEW
// ============================================================================

private data class UITableRowData(val id: Int, val text1: String, val text2: String, val text3: String)

@Preview(widthDp = 500)
@Composable
internal fun UITablePreview() {
    AppTheme {
        Surface(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp)) {
            UiTable(
                data = setOf(
                    UITableRowData(1, "Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData(2, "Texto 1", "Texto 2 Qqwek nnwu", "Texto 3"),
                    UITableRowData(3, "Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData(4, "Texto 1", "Texto 2 dc", "Texto 3B"),
                ).toList(),
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
