package com.eferraz.presentation.design_system.components.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Estado de ordenação da tabela
 */
internal class TableSortState<T> {

    var columnIndex: Int? by mutableStateOf(null)
        internal set
    
    var ascending: Boolean by mutableStateOf(true)
        internal set
    
    /**
     * Atualiza o estado de ordenação
     * @param index Índice da coluna a ordenar
     * @param ascending Se true, ordena de forma crescente
     */
    fun updateSort(index: Int, ascending: Boolean) {
        this.ascending = if (columnIndex != index) true else ascending
        this.columnIndex = index
    }
    
    /**
     * Alterna a direção de ordenação da coluna atual
     */
    fun toggleSort() {
        if (columnIndex != null) {
            ascending = !ascending
        }
    }
    
    /**
     * Reseta o estado de ordenação
     */
    fun reset() {
        columnIndex = null
        ascending = true
    }
}

/**
 * Cria e lembra do estado de ordenação da tabela
 */
@Composable
internal fun <T> rememberTableSortState(
    initialColumnIndex: Int? = null,
    initialAscending: Boolean = true
): TableSortState<T> {
    return remember {
        TableSortState<T>().apply {
            columnIndex = initialColumnIndex
            ascending = initialAscending
        }
    }
}
