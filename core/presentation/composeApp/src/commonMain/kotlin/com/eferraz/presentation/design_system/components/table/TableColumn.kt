package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

/**
 * Tipo para renderização de conteúdo da célula
 */
internal typealias CellContent<T> = @Composable RowScope.(T) -> Unit

/**
 * Tipo para operação de footer
 */
internal typealias FooterOperation<T> = (List<T>) -> String?

/**
 * Configuração de uma coluna da tabela
 * 
 * @param T Tipo do objeto de dados
 * @param sortStrategy Estratégia de ordenação (padrão: não ordenável)
 * @param cellContent Composable para renderizar o conteúdo da célula
 * @param footerOperation Função opcional para calcular o valor do footer
 */
internal data class TableColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val alignment: Alignment.Horizontal = Alignment.Start,
    val sortStrategy: SortStrategy<T> = NotSortableStrategy(),
    val cellContent: CellContent<T>,
    val footerOperation: FooterOperation<T>? = null,
) {

    /**
     * Indica se a coluna é ordenável
     */
    val isSortable: Boolean
        get() = sortStrategy.isSortable()

    companion object
}
