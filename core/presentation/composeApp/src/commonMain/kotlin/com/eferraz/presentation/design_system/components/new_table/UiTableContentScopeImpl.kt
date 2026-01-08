package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

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
        columns.values.map { it.cell }.map { content -> { content(line) } }

    internal fun footerOf(lines: List<T>): List<@Composable (BoxScope.() -> Unit)> =
        columns.values.map { it.footer }.map { content ->
            {
                Text(
                    text = content(lines),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }

    internal fun headerOf(onSelect: (Int) -> Unit): List<@Composable (BoxScope.() -> Unit)> =
        columns.values.mapIndexed { index, entry ->
            {
                Text(
                    text = entry.header,
                    modifier = Modifier.clickable(enabled = entry.isSortable()) { onSelect(index) },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )
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