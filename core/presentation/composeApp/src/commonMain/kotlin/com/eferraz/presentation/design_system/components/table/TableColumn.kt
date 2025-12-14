package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

/**
 * Configuração básica de uma coluna
 */
internal data class ColumnConfig(
    val title: String,
    val weight: Float = 1f,
    val alignment: Alignment.Horizontal = Alignment.Start
)

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
 * @param config Configuração básica da coluna (título, peso, alinhamento)
 * @param sortStrategy Estratégia de ordenação (padrão: não ordenável)
 * @param cellContent Composable para renderizar o conteúdo da célula
 * @param footerOperation Função opcional para calcular o valor do footer
 */
internal data class TableColumn<T>(
    val config: ColumnConfig,
    val sortStrategy: SortStrategy<T> = NotSortableStrategy(),
    val cellContent: CellContent<T> = { item ->
        Text(
            text = item.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    val footerOperation: FooterOperation<T>? = null
) {

    /**
     * Indica se a coluna é ordenável
     */
    val isSortable: Boolean
        get() = sortStrategy.isSortable()
    
    /**
     * Título da coluna
     */
    val title: String
        get() = config.title
    
    /**
     * Peso da coluna para distribuição de largura
     */
    val weight: Float
        get() = config.weight
    
    /**
     * Alinhamento horizontal do conteúdo
     */
    val alignment: Alignment.Horizontal
        get() = config.alignment
}
