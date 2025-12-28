package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.components.inputs.TableInputDate
import com.eferraz.presentation.design_system.components.inputs.TableInputSelect
import com.eferraz.presentation.design_system.components.inputs.TableInputText

/**
 * Cria uma coluna de texto simples (apenas exibição).
 *
 * @param title Título da coluna
 * @param getValue Função que retorna o valor real do campo (usado para ordenação se [sortable] for true)
 * @param format Função opcional para formatar o valor para exibição. Padrão: toString()
 * @param weight Peso da coluna
 * @param alignment Alinhamento do conteúdo
 * @param sortable Se a coluna deve ser ordenável pelo [getValue]
 */
internal fun <T> textColumn(
    title: String,
    getValue: (T) -> Comparable<*>?,
    format: (T) -> String = { getValue(it)?.toString() ?: "" },
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortable: Boolean = true
): TableColumn<T> {
    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = if (sortable) ColumnSortStrategy(getValue) else NotSortableStrategy(),
        cellContent = { item ->
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = format(item),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

/**
 * Cria uma coluna com conteúdo editável genérico.
 *
 * @param title Título da coluna
 * @param weight Peso da coluna
 * @param alignment Alinhamento do conteúdo
 * @param content Composable de conteúdo da célula
 */
internal fun <T> editableColumn(
    title: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable RowScope.(T) -> Unit
): TableColumn<T> {
    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = NotSortableStrategy(),
        cellContent = content
    )
}

// --- Builders Específicos para Inputs ---

/**
 * Cria uma coluna com campo de texto editável.
 *
 * @param title Título da coluna
 * @param getValue Função que obtém o valor atual (para preencher o input)
 * @param onUpdate Função que retorna uma cópia do objeto [T] com o novo valor (String)
 * @param onIntent Função para despachar a intenção de atualização
 */
internal fun <T, I> inputTextColumn(
    title: String,
    getValue: (T) -> String,
    onUpdate: (T, String) -> T,
    onIntent: (I) -> Unit,
    updateIntentFactory: (T) -> I,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortableValue: ((T) -> Comparable<*>?)? = null
): TableColumn<T> {
    
    val strategy = if (sortableValue != null) ColumnSortStrategy(sortableValue) else NotSortableStrategy<T>()

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = strategy,
        cellContent = { item ->
            TableInputText(getValue(item)) { newValue ->
                val updatedItem = onUpdate(item, newValue)
                onIntent(updateIntentFactory(updatedItem))
            }
        }
    )
}

/**
 * Cria uma coluna com campo de texto editável genérico (versão simplificada).
 * Útil quando você já tem uma função de update que lida com intents diretamente ou quando quer mais controle.
 *
 * @param title Título da coluna
 * @param getValue Função que obtém o valor atual
 * @param onValueChange Callback chamado quando o valor muda
 */
internal fun <T> inputTextColumn(
    title: String,
    getValue: (T) -> String,
    onValueChange: (T, String) -> Unit,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortableValue: ((T) -> Comparable<*>?)? = null
): TableColumn<T> {
    
    val strategy = if (sortableValue != null) ColumnSortStrategy(sortableValue) else NotSortableStrategy<T>()

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = strategy,
        cellContent = { item ->
            TableInputText(getValue(item)) { newValue ->
                onValueChange(item, newValue)
            }
        }
    )
}

/**
 * Cria uma coluna com seletor (Dropdown) editável.
 *
 * @param title Título da coluna
 * @param getValue Função que obtém o valor selecionado atual
 * @param options Lista de opções disponíveis
 * @param format Formatação de exibição das opções
 * @param onValueChange Callback chamado quando uma nova opção é selecionada
 */
internal fun <T, V> inputSelectColumn(
    title: String,
    getValue: (T) -> V,
    options: List<V>,
    format: (V) -> String = { it.toString() },
    onValueChange: (T, V) -> Unit,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortableValue: ((T) -> Comparable<*>?)? = null
): TableColumn<T> {
    
    val strategy = if (sortableValue != null) ColumnSortStrategy(sortableValue) else NotSortableStrategy<T>()

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = strategy,
        cellContent = { item ->
            TableInputSelect(
                value = getValue(item),
                options = options,
                format = format,
                onChange = { newValue -> onValueChange(item, newValue) }
            )
        }
    )
}

/**
 * Cria uma coluna com seletor de data editável.
 *
 * @param title Título da coluna
 * @param getValue Função que obtém a data atual (como String formatada)
 * @param onValueChange Callback chamado quando a data muda (retorna String formatada)
 */
internal fun <T> inputDateColumn(
    title: String,
    getValue: (T) -> String,
    onValueChange: (T, String) -> Unit,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortableValue: ((T) -> Comparable<*>?)? = null // Opcional: permite ordenar por data real se fornecido
): TableColumn<T> {
    
    val strategy = if (sortableValue != null) ColumnSortStrategy(sortableValue) else NotSortableStrategy<T>()

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = strategy,
        cellContent = { item ->
            TableInputDate(getValue(item)) { newValue ->
                onValueChange(item, newValue)
            }
        }
    )
}
