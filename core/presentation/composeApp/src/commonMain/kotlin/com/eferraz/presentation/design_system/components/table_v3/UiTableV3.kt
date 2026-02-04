package com.eferraz.presentation.design_system.components.table_v3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.theme.AppTheme
import com.seanproctor.datatable.BasicDataTable
import com.seanproctor.datatable.CellContentProvider
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.DataTableScope
import com.seanproctor.datatable.DataTableState
import com.seanproctor.datatable.TableColumnWidth
import com.seanproctor.datatable.rememberDataTableState

internal interface UiTableContentProvider {

    @Composable
    fun DefaultHeader(text: String)

    @Composable
    fun DefaultCell(text: String)

    @Composable
    fun DefaultFooter(text: String)
}

internal object UiTableContentProviderImpl : UiTableContentProvider {

    @Composable
    override fun DefaultHeader(
        text: String,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun DefaultCell(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun DefaultFooter(text: String) {
        Text(
            text = "Footer",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
internal fun UiTableV3(
    modifier: Modifier = Modifier,
    contentProvider: UiTableContentProvider = UiTableContentProviderImpl,
    header: List<UiTableDataColumn>,
    data: List<List<Any>>,
    footer: @Composable () -> Unit = {},
) {

    val state = rememberDataTableState()

    var sortConfig by remember { mutableStateOf<Pair<Int?, Boolean>>(Pair(null, true)) }

    val sortedData = remember(data, sortConfig) {
        when {
            sortConfig.first == null -> data
            sortConfig.second -> data.sortedBy { it.getOrNull(sortConfig.first!!)?.toString().orEmpty() }
            else -> data.sortedByDescending { it.getOrNull(sortConfig.first!!)?.toString().orEmpty() }
        }
    }

    val columns = header.map { columnHeader ->
        DataColumn(
            width = columnHeader.width,
            alignment = columnHeader.alignment,
            onSort = { columnIndex: Int, ascending: Boolean -> sortConfig = columnIndex to ascending },
            header = { contentProvider.DefaultHeader(columnHeader.text) }
        )
    }

    val content: DataTableScope.() -> Unit = {
        sortedData.forEach { rowData ->
            row {
                rowData.forEach { cellData ->
                    cell {
                        contentProvider.DefaultCell(cellData.toString())
                    }
                }
            }
        }
    }

    val headerBackgroundColor = Color.LightGray
    val footerBackgroundColor = Color.Blue.copy(alpha = 0.2f)

    DataTable(
        modifier = modifier,
        state = state,
        sortAscending = sortConfig.second,
        sortColumnIndex = sortConfig.first,
        headerBackgroundColor = headerBackgroundColor,
        footerBackgroundColor = footerBackgroundColor,
        columns = columns,
        content = content,
        footer = footer
    )
}

@Composable
private fun DataTable(
    columns: List<DataColumn>,
    modifier: Modifier = Modifier,
    state: DataTableState = rememberDataTableState(),
    separator: @Composable () -> Unit = { HorizontalDivider() },
    headerHeight: Dp = 56.dp,
    rowHeight: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    headerBackgroundColor: Color = Color.Unspecified,
    footerBackgroundColor: Color = Color.Unspecified,
    footer: @Composable () -> Unit = { },
    sortColumnIndex: Int? = null,
    sortAscending: Boolean = true,
    logger: ((String) -> Unit)? = { println("Table: $it") },
    content: DataTableScope.() -> Unit,
) {

    Surface {
        BasicDataTable(
            columns = columns,
            modifier = modifier,
            state = state,
            separator = separator,
            headerHeight = headerHeight,
            rowHeight = rowHeight,
            contentPadding = contentPadding,
            headerBackgroundColor = headerBackgroundColor,
            footerBackgroundColor = footerBackgroundColor,
            footer = footer,
            cellContentProvider = Material3CellContentProvider,
            sortColumnIndex = sortColumnIndex,
            sortAscending = sortAscending,
            logger = logger,
            content = content
        )
    }
}

private object Material3CellContentProvider : CellContentProvider {

    @Composable
    override fun RowCellContent(content: @Composable () -> Unit) {

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            content()
        }
    }

    @Composable
    override fun HeaderCellContent(
        sorted: Boolean,
        sortAscending: Boolean,
        isSortIconTrailing: Boolean,
        onClick: (() -> Unit)?,
        content: @Composable () -> Unit,
    ) {

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {

            Row(
                modifier = Modifier.clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                content()

                if (sorted && onClick != null) {
                    Icon(
                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

internal data class UiTableDataColumn(
    val text: String,
    val alignment: Alignment = Alignment.CenterStart,
    val width: TableColumnWidth = TableColumnWidth.Flex(1f),
)

@Preview(widthDp = 700)
@Composable
internal fun UiTableV3Preview() {

    AppTheme {

        Surface {

            // Dados para demonstrar ordenação
            val previewData = listOf(
                listOf(5, "Zebra", "Item Z", 100),
                listOf(2, "Apple", "Item A", 50),
                listOf(8, "Mango", "Item M", 75),
                listOf(1, "Banana", "Item B", 25),
                listOf(3, "Cherry", "Item C", 90),
                listOf(6, "Orange", "Item O", 60),
            )

            UiTableV3(
                header = listOf(
                    UiTableDataColumn("ID", width = TableColumnWidth.MaxIntrinsic, alignment = Alignment.Center),
                    UiTableDataColumn("Nome", width = TableColumnWidth.MaxIntrinsic),
                    UiTableDataColumn("Descrição", width = TableColumnWidth.Flex(1f)),
                    UiTableDataColumn("Valor", width = TableColumnWidth.MaxIntrinsic, alignment = Alignment.CenterEnd),
                ),
                data = previewData,
                footer = {
                    Text(
                        text = "Total: ${previewData.size} itens",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            )
        }
    }
}