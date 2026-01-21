package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ============================================================================
// API PÚBLICA - SCOPE DSL
// ============================================================================

public interface UiTableContentScope<T> {

    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        weight: Float = 1.0f,
        alignment: Alignment.Horizontal = Alignment.Start,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: ((List<T>) -> String)? = null,
    )

    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        weight: Float = 1.0f,
        alignment: Alignment.Horizontal = Alignment.Start,
        cellValue: @Composable (T) -> String,
        footer: ((List<T>) -> String)? = null,
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
        weight: Float,
        alignment: Alignment.Horizontal,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: ((List<T>) -> String)?,
    ) {
        columns.add(ColumnData(header, sortedBy, weight, alignment, cellContent, footer))
    }

    override fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)?,
        weight: Float,
        alignment: Alignment.Horizontal,
        cellValue: @Composable (T) -> String,
        footer: ((List<T>) -> String)?,
    ) {

        val textAlign = when (alignment) {
            Alignment.Start -> TextAlign.Start
            Alignment.CenterHorizontally -> TextAlign.Center
            Alignment.End -> TextAlign.End
            else -> TextAlign.Start
        }

        columns.add(
            ColumnData(
                header = header,
                sortedBy = sortedBy,
                weight = weight,
                alignment = alignment,
                cell = @Composable { current ->
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = cellValue(current),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = textAlign,
                        softWrap = false
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
    val weight: Float,
    val alignment: Alignment.Horizontal,
    val cell: @Composable BoxScope.(T) -> Unit,
    val footer: ((List<T>) -> String)?,
) {

    fun isSortable() = sortedBy != null

    fun hasFooter() = footer != null
}

