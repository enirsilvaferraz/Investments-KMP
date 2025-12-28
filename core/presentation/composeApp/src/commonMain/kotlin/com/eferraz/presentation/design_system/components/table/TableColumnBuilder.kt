package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal fun <T> inputTextCell(
    formated: T.() -> String,
): @Composable RowScope.(T) -> Unit = {
    Text(
        modifier = Modifier.padding(horizontal = 8.dp),
        text = formated(it),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Função de compatibilidade com a API antiga
 * Cria uma TableColumn a partir dos parâmetros antigos
 */
@Deprecated(
    "Use TableColumn.Text, TableColumn.InputText, etc. instead for simpler API and better separation of concerns.",
    ReplaceWith("TableColumn.Text(title, { data() })")
)
internal fun <T> TableColumn(
    title: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    data: (T.() -> Comparable<*>?),
    formated: T.() -> String = { data().toString() },
    cellContent: @Composable RowScope.(T) -> Unit = inputTextCell(formated),
    footerOperation: ((List<T>) -> String?)? = null,
): TableColumn<T> {

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = ColumnSortStrategy { item -> data(item) },
        cellContent = cellContent,
        footerOperation = footerOperation
    )
}