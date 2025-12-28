package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Builder para facilitar a criação de TableColumn
 * Mantém compatibilidade com a API antiga
 */
internal class ColumnBuilder<T> {

    var title: String = ""
    var weight: Float = 1f
    var alignment: Alignment.Horizontal = Alignment.Start
    var sortStrategy: SortStrategy<T> = NotSortableStrategy()
    var cellContent: CellContent<T>? = null
    var footerOperation: FooterOperation<T>? = null
    var formatter: (T) -> String? = { null }

    /**
     * Define a estratégia de ordenação usando um extrator
     */
    fun sortable(extractor: (T) -> Comparable<*>?) {
        sortStrategy = ColumnSortStrategy(extractor)
    }

    /**
     * Define o conteúdo customizado da célula
     */
    fun cellContent(content: @Composable RowScope.(T) -> Unit) {
        this.cellContent = content
    }

    /**
     * Define a operação de footer
     */
    fun footer(operation: (List<T>) -> String?) {
        this.footerOperation = operation
    }

    /**
     * Define o formatador padrão (para compatibilidade com API antiga)
     */
    fun formated(formatter: (T) -> String) {
        this.formatter = formatter
    }

    /**
     * Constrói a TableColumn
     */
    fun build(): TableColumn<T> {
        val config = ColumnConfig(
            title = title,
            weight = weight,
            alignment = alignment
        )

        val content = cellContent ?: run {
            { item: T ->
                val text = formatter(item) ?: item.toString()
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        return TableColumn(
            config = config,
            sortStrategy = sortStrategy,
            cellContent = content,
            footerOperation = footerOperation
        )
    }
}

/**
 * Função de conveniência para criar uma TableColumn usando builder DSL
 */
internal fun <T> tableColumn(
    title: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    block: ColumnBuilder<T>.() -> Unit = {}
): TableColumn<T> {
    return ColumnBuilder<T>().apply {
        this.title = title
        this.weight = weight
        this.alignment = alignment
        block()
    }.build()
}

/**
 * Função de compatibilidade com a API antiga
 * Cria uma TableColumn a partir dos parâmetros antigos
 */
internal fun <T> TableColumn(
    title: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    data: (T.() -> Comparable<*>?),
    formated: T.() -> String = { data().toString() },
    cellContent: @Composable RowScope.(T) -> Unit = {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = formated(it),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    footerOperation: ((List<T>) -> String?)? = null,
): TableColumn<T> {

    val config = ColumnConfig(
        title = title,
        weight = weight,
        alignment = alignment
    )

    val sortStrategy = ColumnSortStrategy<T> { item ->
        data(item)
    }

    return TableColumn(
        config = config,
        sortStrategy = sortStrategy,
        cellContent = cellContent,
        footerOperation = footerOperation
    )
}
