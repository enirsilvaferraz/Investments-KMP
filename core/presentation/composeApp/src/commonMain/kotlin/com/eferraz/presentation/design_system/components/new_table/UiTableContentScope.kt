package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ============================================================================
// API PÚBLICA - SCOPE DSL
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

// ============================================================================
// IMPLEMENTAÇÃO INTERNA
// ============================================================================

@Stable
internal class UiTableScope<T> : UiTableContentScope<T> {

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
                        modifier = Modifier.padding(horizontal = 8.dp),
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
internal data class ColumnData<T>(
    val header: String,
    val sortedBy: ((T) -> Comparable<*>)?,
    val cell: @Composable BoxScope.(T) -> Unit,
    val footer: (List<T>) -> String,
) {
    fun isSortable() = sortedBy != null
}

