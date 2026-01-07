package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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

private data object UiTableScope {
    lateinit var columnWidths: SnapshotStateMap<Int, Int>
}

@Composable
internal fun <T> UiTable(
    modifier: Modifier,
    data: List<T>,
    contentPadding: PaddingValues = PaddingValues(),
    content: UiTableContentScope<T>.() -> Unit,
) {

    with(UiTableScope) {

        columnWidths = remember { mutableStateMapOf() }

        with(UiTableContentScopeImpl<T>().apply(content)) {

            var state by remember(data, columns) {
                mutableStateOf(UiTableSortState(data = data, columns = columns.values.map { it.sortedBy }))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()//.horizontalScroll(rememberScrollState())
            ) {

                // 1. STICKY HEADER
                stickyHeader {
                    UiTableRow(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        content = headerOf { state = state.sort(it) },
                    )
                }

                // 2. ITEMS
                items(state.sortedData) { item ->
                    UiTableRow(
                        modifier = Modifier,
                        content = lineOf(item),
                        showDivider = state.sortedData.last() != item
                    )
                }

                // 3. FOOTER
                item {
                    UiTableRow(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        content = footerOf(data),
                    )
                }
            }
        }
    }
}

@Composable
private fun UiTableScope.UiTableRow(
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    content: List<@Composable BoxScope.() -> Unit>,
) {
    Column {

        Row(
            modifier = modifier.height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            content.forEachIndexed { index, content ->
                UiTableCell(index, content)
            }
        }

        if (showDivider) {

            val larguraAtualDp = with(LocalDensity.current) { columnWidths.values.sum().toDp() }

            HorizontalDivider(
                modifier = Modifier.width(larguraAtualDp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun UiTableScope.UiTableCell(
    index: Int,
    content: @Composable BoxScope.() -> Unit,
) {

    val larguraAtualDp = with(LocalDensity.current) { columnWidths[index]?.toDp() }

    Box(
        modifier = Modifier
            .then(if (larguraAtualDp != null) Modifier.width(larguraAtualDp) else Modifier)
            .onGloballyPositioned { coordinates ->
                if (coordinates.size.width > (columnWidths[index] ?: 0)) columnWidths[index] = coordinates.size.width
            },
    ) {

        Box(modifier = Modifier.padding(8.dp), content = content)
    }
}

private data class UITableRowData(val text1: String, val text2: String, val text3: String)

@Preview(widthDp = 500)
@Composable
internal fun UITablePreview() {
    AppTheme {
        Surface {
            UiTable(
                modifier = Modifier.padding(16.dp),
                data = listOf(
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2 Qqwek nnwu", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2 dc", "Texto 3B he"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3A"),
                )
            ) {

                column(
                    header = "Header 1",
                    cellContent = @Composable { Text(it.text1) }
                )

                column(
                    header = "Header 2",
                    sortedBy = { it.text2 },
                    cellContent = @Composable { Text(it.text2) },
                    footer = { "Footer teste teste teste teste" }
                )

                column(
                    header = "Header 3",
                    sortedBy = { it.text3 },
                    cellContent = @Composable { Text(it.text3) }
                )

                column(
                    header = "Header 4",
                    cellValue = @Composable { it.text1 },
                    footer = { it.size.toString() }
                )
            }
        }
    }
}