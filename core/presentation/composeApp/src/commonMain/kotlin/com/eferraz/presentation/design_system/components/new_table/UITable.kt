package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.theme.AppTheme

@Composable
internal fun <T> UiTable(
    modifier: Modifier = Modifier,
    data: List<T>,
    contentPadding: PaddingValues = PaddingValues(),
    content: UiTableContentScope<T>.() -> Unit,
) {

    with(UiTableContentScopeImpl<T>().apply(content)) {

        val sortState = rememberUiTableSortState(data = data, columns = columns.values.map { it.sortedBy })
        val responsiveState = rememberUiTableResponsiveState(columnCount = columns.size)

        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {

            // 1. STICKY HEADER
            stickyHeader {
                UiTableResponsiveRow(
                    state = responsiveState,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    content = headerOf { sortState.sort(it) },
                )
            }

            // 2. ITEMS
            itemsIndexed(sortState.sortedData) { index, item ->
                UiTableResponsiveRow(
                    state = responsiveState,
                    modifier = Modifier,
                    showDivider = index < sortState.sortedData.size - 1,
                    content = lineOf(item),
                )
            }

            // 3. FOOTER
            item {
                UiTableResponsiveRow(
                    state = responsiveState,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    content = footerOf(data),
                )
            }
        }
    }
}


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
                    UITableRowData("Texto 1", "Texto 2", "Texto 3"),
                    UITableRowData("Texto 1", "Texto 2", "Texto 3A"),
                )
            ) {

                column(
                    header = "Header 1",
                    cellValue = { it.text1 }
                )

                column(
                    header = "Header 2",
                    sortedBy = { it.text2 },
                    cellValue = { it.text2 },
                    footer = { "Footer teste teste teste teste" }
                )

                column(
                    header = "Header 3",
                    sortedBy = { it.text3 },
                    cellValue = { it.text3 }
                )

                column(
                    header = "Header 4",
                    cellValue = { it.text1 },
                    footer = { it.size.toString() }
                )
            }
        }
    }
}