package com.eferraz.presentation.design_system.components.table

/**
 * Função pura para ordenar dados da tabela
 * 
 * @param data Lista de dados a ordenar
 * @param columns Lista de colunas da tabela
 * @param sortState Estado de ordenação atual
 * @return Lista ordenada ou lista original se não houver ordenação válida
 */
internal fun <T> sortData(
    data: List<T>,
    columns: List<TableColumn<T>>,
    sortState: TableSortState<T>
): List<T> {

    val columnIndex = sortState.columnIndex ?: return data
    
    // Validação de índice
    if (columnIndex < 0 || columnIndex >= columns.size) return data
    
    val column = columns[columnIndex]
    
    // Verifica se a coluna é ordenável
    if (!column.isSortable) return data
    
    return column.sortStrategy.sort(data, sortState.ascending)
}
