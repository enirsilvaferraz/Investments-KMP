package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal class UiTableContentScopeImpl<T> internal constructor() : UiTableContentScope<T> {

    internal val columns: MutableMap<Int, UiColumnData<T>> = mutableMapOf()

    override fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)?,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: (List<T>) -> String,
    ) {

        columns[columns.size] = UiColumnData(
            header = header,
            cell = cellContent,
            footer = footer,
            sortedBy = sortedBy
        )
    }

    override fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)?,
        cellValue: @Composable (T) -> String,
        footer: (List<T>) -> String,
    ) {

        columns[columns.size] = UiColumnData(
            header = header,
            cell = @Composable { current ->
                Text(
                    text = cellValue(current),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            footer = footer,
            sortedBy = sortedBy
        )
    }

    internal fun lineOf(line: T): List<@Composable (BoxScope.() -> Unit)> =
        columns.values.map { it.cell }.map { content ->
            {
                Box(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    contentAlignment = Alignment.CenterStart
                ) { content(line) }
            }
        }

    internal fun footerOf(lines: List<T>): List<@Composable (BoxScope.() -> Unit)> =
        columns.values.map { it.footer }.map { content ->
            {
                Box(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = content(lines),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }

    internal fun headerOf(
        sortedColumnIndex: Int,
        isAscending: Boolean,
        onSelect: (Int) -> Unit,
    ): List<@Composable (BoxScope.() -> Unit)> =
        columns.values.mapIndexed { index, entry ->
            {

                Row(
                    modifier = Modifier.fillMaxWidth().height(54.dp).clickable(enabled = entry.isSortable()) { onSelect(index) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = entry.header,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )

                    if (sortedColumnIndex == index)
                        Icon(
                            imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                }

            }
        }

    internal data class UiColumnData<T>(
        val header: String,
        val sortedBy: ((T) -> Comparable<*>)?,
        val cell: @Composable BoxScope.(T) -> Unit,
        val footer: (List<T>) -> String,
    ) {
        fun isSortable() = sortedBy != null
    }
}