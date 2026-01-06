package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.theme.AppTheme

internal class UiTableScope<T> {

    val columns: MutableMap<Int, UiColumnData<T>> = mutableMapOf()

    fun column(
        header: String,
        cell: @Composable BoxScope.(T) -> Unit,
        footer: String = "",
        sortedBy: ((T) -> Comparable<*>)? = null,
    ) {

        columns[columns.size] = UiColumnData(
            header = header,
            cell = cell,
            footer = footer,
            sortedBy = sortedBy
        )
    }

    internal data class UiColumnData<T>(
        val header: String,
        val cell: @Composable BoxScope.(T) -> Unit,
        val footer: String,
        val sortedBy: ((T) -> Comparable<*>)?,
    )
}

private data object UiTableState {
    lateinit var columnWidths: SnapshotStateMap<Int, Int>
}

@Composable
internal fun <T> UiTable(
    modifier: Modifier,
    data: List<T>,
    contentPadding: PaddingValues = PaddingValues(),
    content: UiTableScope<T>.() -> Unit,
) {

    with(UiTableState) {

        columnWidths = remember { mutableStateMapOf() }
        
        var sortedColumnIndex by remember { mutableStateOf<Int?>(null) }
        var isAscending by remember { mutableStateOf(true) }

        with(UiTableScope<T>().apply(content)) {
            
            val sortedData = remember(data, sortedColumnIndex, isAscending, columns) {
                if (sortedColumnIndex == null) {
                    data
                } else {
                    val columnData = columns[sortedColumnIndex]
                    val sortedByFunction = columnData?.sortedBy
                    if (sortedByFunction != null) {
                        val comparator = compareBy<T> { sortedByFunction(it) }
                        if (isAscending) {
                            data.sortedWith(comparator)
                        } else {
                            data.sortedWith(comparator.reversed())
                        }
                    } else {
                        data
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState())
            ) {

                // 1. STICKY HEADER
                stickyHeader {
                    LinhaTabela(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        content = columns.entries.mapIndexed { index, entry ->
                            {
                                val columnData = entry.value
                                val isSortable = columnData.sortedBy != null
                                val isCurrentSortedColumn = sortedColumnIndex == index
                                val headerText = if (isCurrentSortedColumn && isSortable) {
                                    "${columnData.header} ${if (isAscending) "↑" else "↓"}"
                                } else {
                                    columnData.header
                                }
                                Text(
                                    headerText,
                                    modifier = if (isSortable) {
                                        Modifier.clickable {
                                            if (sortedColumnIndex == index) {
                                                isAscending = !isAscending
                                            } else {
                                                sortedColumnIndex = index
                                                isAscending = true
                                            }
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                            }
                        },
                    )
                }

                // 2. ITEMS
                items(sortedData) { t ->
                    LinhaTabela(
                        content = columns.values.map { it.cell }.map { content -> { this.content(t) } },
                    )
                }

                // 3. FOOTER
                item {
                    LinhaTabela(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        content = columns.values.map { content -> { Text(content.footer) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun UiTableState.LinhaTabela(
    modifier: Modifier = Modifier,
    content: List<@Composable BoxScope.() -> Unit>,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        content.forEachIndexed { index, content ->
            UiTableCell(index, content)
        }
    }
}

@Composable
private fun UiTableState.UiTableCell(
    index: Int,
    content: @Composable BoxScope.() -> Unit,
) {

    val larguraAtualDp = with(LocalDensity.current) { columnWidths[index]?.toDp() }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .then(if (larguraAtualDp != null) Modifier.width(larguraAtualDp) else Modifier)
            .onGloballyPositioned { coordinates ->
                if (coordinates.size.width > (columnWidths[index] ?: 0)) columnWidths[index] = coordinates.size.width
            },
        content = content
    )
}

private data class UITableRowData(val text1: String, val text2: String, val text3: String)

@Preview(widthDp = 500)
@Composable
private fun UITablePreview() {
    AppTheme {
        Surface {
            UiTable(
                modifier = Modifier.padding(16.dp),
                data = listOf(
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2 Qqwek nnwu", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                )
            ) {

                column(
                    header = "Header 1",
                    cell = { Text(it.text1) }
                )

                column(
                    header = "Header 2",
                    cell = { Text(it.text2) },
                    footer = "Footer teste teste teste teste"
                )

                column(
                    header = "Header 3",
                    cell = { Text(it.text3) }
                )

                column(
                    header = "Header 4",
                    cell = { Text(it.text1) },
                    footer = "A"
                )
            }
        }
    }
}