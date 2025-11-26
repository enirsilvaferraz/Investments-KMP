package com.eferraz.presentation.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Configuração de uma coluna da tabela
 *
 * @param T Tipo do objeto de dados
 * @param title Título da coluna exibido no cabeçalho
 * @param weight Peso da coluna para distribuição de largura (padrão 1f)
 * @param alignment Alinhamento horizontal do conteúdo da coluna
 * @param formated Função para extrair valor como String (para ordenação e exibição padrão)
 * @param data Função opcional para comparar itens durante ordenação. Se null, a coluna não é ordenável
 * @param cellContent Composable customizado para renderizar o conteúdo da célula
 */
internal data class TableColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val alignment: Alignment.Horizontal = Alignment.Start,
    val data: (T.() -> Comparable<*>?),
    val formated: T.() -> String = { data().toString() },
    val cellContent: @Composable RowScope.(T) -> Unit = {
        Text(
            text = formated(it),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
        )
    },
)

/**
 * Componente de tabela de dados nativo seguindo especificações do Material Design 2
 * Conforme: https://m2.material.io/components/data-tables#theming
 *
 * @param T Tipo do objeto de dados
 * @param modifier Modifier para customização
 * @param columns Lista de configurações de colunas
 * @param data Lista de dados a exibir
 * @param sortColumnIndex Índice da coluna inicialmente ordenada (padrão: 0)
 * @param onRowClick Callback opcional chamado quando uma linha é clicada
 */
@Composable
internal fun <T> DataTable(
    modifier: Modifier = Modifier,
    columns: List<TableColumn<T>>,
    data: List<T>,
    sortColumnIndex: Int = 0,
    onRowClick: ((T) -> Unit)? = null,
) {

    var sortColumnIndex by remember { mutableStateOf(sortColumnIndex) }
    var sortAscending by remember { mutableStateOf(true) }

    val onSort: ((Int, Boolean) -> Unit) = { index, ascending ->
        sortAscending = if (sortColumnIndex != index) true else ascending
        sortColumnIndex = index
    }

    val sortedData = remember(data, sortColumnIndex, sortAscending, columns) {
        val comparator = columns[sortColumnIndex].data
        if (sortAscending) data.sortedWith(compareBy { item -> comparator(item) })
        else data.sortedWith(compareByDescending { item -> comparator(item) })
    }

    val colors = MaterialTheme.colorScheme
    val headerBackgroundColor = colors.surfaceContainerHigh
    val headerTextColor = colors.onSurface.copy(alpha = 0.87f)
    val evenRowColor = colors.surface
    val oddRowColor = colors.surfaceContainerLow
    val dividerColor = colors.outline.copy(alpha = 0.12f)
    val sortIconColor = colors.primary

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // Cabeçalho da tabela
        stickyHeader {
            TableHeader(
                columns = columns,
                sortColumnIndex = sortColumnIndex,
                sortAscending = sortAscending,
                onSort = onSort,
                backgroundColor = headerBackgroundColor,
                textColor = headerTextColor,
                sortIconColor = sortIconColor
            )
        }

        // Linhas de dados
        itemsIndexed(sortedData) { index, item ->
            TableRow(
                item = item,
                columns = columns,
                isEven = index % 2 == 0,
                backgroundColor = if (index % 2 == 0) oddRowColor else evenRowColor,
                dividerColor = dividerColor,
                onRowClick = onRowClick,
                showDivider = index < sortedData.size - 1
            )
        }
    }
}

/**
 * Cabeçalho da tabela
 */
@Composable
private fun <T> TableHeader(
    columns: List<TableColumn<T>>,
    sortColumnIndex: Int?,
    sortAscending: Boolean,
    onSort: ((Int, Boolean) -> Unit),
    backgroundColor: Color,
    textColor: Color,
    sortIconColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        columns.forEachIndexed { index, column ->

            val isSorted = sortColumnIndex == index

            Box(
                modifier = Modifier
                    .weight(column.weight)
                    .clickable {
                        onSort.invoke(index, if (isSorted) !sortAscending else true)
                    },
                contentAlignment = when (column.alignment) {
                    Alignment.Start -> Alignment.CenterStart
                    Alignment.CenterHorizontally -> Alignment.Center
                    Alignment.End -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = textColor
                    )

                    if (isSorted) Icon(
                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = sortIconColor
                    )
                }
            }
        }
    }
}

/**
 * Linha de dados da tabela
 */
@Composable
private fun <T> TableRow(
    item: T,
    columns: List<TableColumn<T>>,
    isEven: Boolean,
    backgroundColor: Color,
    dividerColor: Color,
    onRowClick: ((T) -> Unit)?,
    showDivider: Boolean,
) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .heightIn(min = 52.dp)
                .then(if (onRowClick != null) Modifier.clickable { onRowClick(item) } else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                TableCell(
                    item = item,
                    column = column
                )
            }
        }

        if (showDivider) HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = dividerColor,
            thickness = 1.dp
        )
    }
}

/**
 * Célula individual da tabela
 */
@Composable
private fun <T> RowScope.TableCell(
    item: T,
    column: TableColumn<T>,
) {
    Box(
        modifier = Modifier.weight(column.weight),
        contentAlignment = when (column.alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.CenterHorizontally -> Alignment.Center
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        column.cellContent.invoke(this@TableCell, item)
    }
}

