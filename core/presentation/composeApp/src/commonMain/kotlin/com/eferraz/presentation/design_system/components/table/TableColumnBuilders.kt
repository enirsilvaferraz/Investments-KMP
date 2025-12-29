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
import com.eferraz.presentation.design_system.components.inputs.TableInputMoney
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
 * @param footerOperation Função opcional para calcular o valor do footer
 */
internal fun <T> editableColumn(
    title: String,
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable RowScope.(T) -> Unit,
    footerOperation: ((List<T>) -> String?)? = null
): TableColumn<T> {
    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = NotSortableStrategy(),
        cellContent = content,
        footerOperation = footerOperation
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

/**
 * Cria uma coluna com campo de dinheiro editável.
 *
 * @param title Título da coluna
 * @param getValue Função que obtém o valor atual (Double)
 * @param onValueChange Callback chamado quando o valor muda (retorna Double?)
 * @param getEnabled Função opcional que determina se o campo está habilitado (padrão: sempre habilitado)
 * @param weight Peso da coluna
 * @param alignment Alinhamento do conteúdo
 * @param sortableValue Função opcional para ordenação (padrão: não ordenável)
 * @param footerOperation Função opcional para calcular o valor do footer
 */
internal fun <T> inputMoneyColumn(
    title: String,
    getValue: (T) -> Double,
    onValueChange: (T, Double?) -> Unit,
    getEnabled: (T) -> Boolean = { true },
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortableValue: ((T) -> Comparable<*>?)? = null,
    footerOperation: ((List<T>) -> String?)? = null
): TableColumn<T> {
    
    val strategy = if (sortableValue != null) ColumnSortStrategy(sortableValue) else NotSortableStrategy<T>()

    return TableColumn(
        title = title,
        weight = weight,
        alignment = alignment,
        sortStrategy = strategy,
        cellContent = { item ->
            TableInputMoney(
                value = getValue(item),
                onValueChange = { newValue -> onValueChange(item, newValue) },
                enabled = getEnabled(item)
            )
        },
        footerOperation = footerOperation
    )
}

// --- Builders de Footer ---

/**
 * Adiciona uma operação de footer a uma coluna existente.
 * 
 * @param footerOperation Função que calcula o valor do footer a partir da lista de dados
 * @return Nova coluna com o footer adicionado
 */
internal fun <T> TableColumn<T>.withFooter(
    footerOperation: (List<T>) -> String?
): TableColumn<T> {
    return this.copy(footerOperation = footerOperation)
}

/**
 * Adiciona um footer de soma numérica a uma coluna.
 * 
 * @param extractor Função que extrai o valor numérico de cada item
 * @param formatter Função opcional para formatar o resultado (padrão: toString())
 * @return Nova coluna com o footer de soma
 */
internal fun <T> TableColumn<T>.withSumFooter(
    extractor: (T) -> Number,
    formatter: (Double) -> String = { it.toString() }
): TableColumn<T> {
    return this.copy(
        footerOperation = { data ->
            if (data.isEmpty()) null
            else formatter(data.sumOf { extractor(it).toDouble() })
        }
    )
}

/**
 * Adiciona um footer de média numérica a uma coluna.
 * 
 * @param extractor Função que extrai o valor numérico de cada item
 * @param formatter Função opcional para formatar o resultado (padrão: toString())
 * @return Nova coluna com o footer de média
 */
internal fun <T> TableColumn<T>.withAverageFooter(
    extractor: (T) -> Number,
    formatter: (Double) -> String = { it.toString() }
): TableColumn<T> {
    return this.copy(
        footerOperation = { data ->
            if (data.isEmpty()) null
            else formatter(data.map { extractor(it).toDouble() }.average())
        }
    )
}

/**
 * Adiciona um footer de contagem a uma coluna.
 * 
 * @param formatter Função opcional para formatar o resultado (padrão: toString())
 * @return Nova coluna com o footer de contagem
 */
internal fun <T> TableColumn<T>.withCountFooter(
    formatter: (Int) -> String = { it.toString() }
): TableColumn<T> {
    return this.copy(
        footerOperation = { data ->
            if (data.isEmpty()) null
            else formatter(data.size)
        }
    )
}

/**
 * Adiciona um footer de texto customizado a uma coluna.
 * 
 * @param text Texto fixo a exibir no footer
 * @return Nova coluna com o footer de texto
 */
internal fun <T> TableColumn<T>.withTextFooter(
    text: String
): TableColumn<T> {
    return this.copy(
        footerOperation = { if (it.isEmpty()) null else text }
    )
}

/**
 * Versão de textColumn com suporte a footer.
 * 
 * @param title Título da coluna
 * @param getValue Função que retorna o valor real do campo (usado para ordenação se [sortable] for true)
 * @param format Função opcional para formatar o valor para exibição. Padrão: toString()
 * @param weight Peso da coluna
 * @param alignment Alinhamento do conteúdo
 * @param sortable Se a coluna deve ser ordenável pelo [getValue]
 * @param footerOperation Função opcional para calcular o valor do footer
 */
internal fun <T> textColumn(
    title: String,
    getValue: (T) -> Comparable<*>?,
    format: (T) -> String = { getValue(it)?.toString() ?: "" },
    weight: Float = 1f,
    alignment: Alignment.Horizontal = Alignment.Start,
    sortable: Boolean = true,
    footerOperation: ((List<T>) -> String?)? = null
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
        },
        footerOperation = footerOperation
    )
}
