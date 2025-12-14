package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Componente de tabela de dados nativo seguindo especificações do Material Design 3
 *
 * @param T Tipo do objeto de dados
 * @param modifier Modifier para customização
 * @param contentPadding Padding do conteúdo
 * @param columns Lista de configurações de colunas
 * @param data Lista de dados a exibir
 * @param sortState Estado de ordenação (padrão: criado automaticamente)
 * @param theme Tema visual da tabela (padrão: MaterialTableTheme)
 * @param onRowClick Callback opcional chamado quando uma linha é clicada
 */
@Composable
internal fun <T> DataTable(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    columns: List<TableColumn<T>>,
    data: List<T>,
    sortState: TableSortState<T> = rememberTableSortState(),
    theme: TableTheme = MaterialTableTheme(),
    onRowClick: ((T) -> Unit)? = null,
) {
    // Ordena os dados usando função pura
    val sortedData by remember(data, sortState.columnIndex, sortState.ascending, columns) {
        derivedStateOf {
            sortData(data, columns, sortState)
        }
    }

    Column {

        LazyColumn(
            modifier = modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = contentPadding
        ) {

            // Cabeçalho da tabela
            stickyHeader {
                TableHeader(
                    columns = columns,
                    sortState = sortState,
                    theme = theme,
                    onSort = { index, ascending ->
                        sortState.updateSort(index, ascending)
                    }
                )
            }

            // Linhas de dados
            itemsIndexed(sortedData) { index, item ->
                TableRow(
                    item = item,
                    columns = columns,
                    backgroundColor = if (index % 2 == 0) theme.oddRowBackground else theme.evenRowBackground,
                    showDivider = index < sortedData.size - 1,
                    dividerColor = theme.divider,
                    onRowClick = onRowClick
                )
            }
        }

        // Footer (se houver operações de footer)
        val hasFooterOperation = columns.any { it.footerOperation != null }

        if (hasFooterOperation) {

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = theme.divider,
                thickness = 1.dp
            )

            // Usa sortedData no footer em vez de data original (correção de bug)
            TableFooter(
                columns = columns,
                data = sortedData,
                dividerColor = theme.divider
            )
        }
    }
}

internal fun <T> alignment(column: TableColumn<T>): Alignment = when (column.alignment) {
    Alignment.Start -> Alignment.CenterStart
    Alignment.CenterHorizontally -> Alignment.Center
    Alignment.End -> Alignment.CenterEnd
    else -> Alignment.CenterStart
}
