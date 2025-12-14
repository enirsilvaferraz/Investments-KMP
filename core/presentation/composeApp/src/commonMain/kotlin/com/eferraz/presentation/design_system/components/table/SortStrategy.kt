package com.eferraz.presentation.design_system.components.table

/**
 * Estratégia de ordenação para uma coluna da tabela
 */
internal interface SortStrategy<T> {
    /**
     * Ordena a lista de dados
     * @param data Lista de dados a ordenar
     * @param ascending Se true, ordena de forma crescente; caso contrário, decrescente
     * @return Lista ordenada
     */
    fun sort(data: List<T>, ascending: Boolean): List<T>
    
    /**
     * Indica se a coluna é ordenável
     */
    fun isSortable(): Boolean
}

/**
 * Estratégia de ordenação baseada em um extrator de valor comparável
 */
internal class ColumnSortStrategy<T>(
    private val extractor: (T) -> Comparable<*>?
) : SortStrategy<T> {
    
    override fun sort(data: List<T>, ascending: Boolean): List<T> {
        val comparator = Comparator<T> { a, b ->
            val aValue = extractor(a)
            val bValue = extractor(b)
            when {
                aValue == null && bValue == null -> 0
                aValue == null -> 1 // nulls last
                bValue == null -> -1 // nulls last
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    (aValue as Comparable<Any>).compareTo(bValue as Comparable<Any>)
                }
            }
        }
        
        return if (ascending) {
            data.sortedWith(comparator)
        } else {
            data.sortedWith(comparator.reversed())
        }
    }
    
    override fun isSortable(): Boolean = true
}

/**
 * Estratégia que indica que a coluna não é ordenável
 */
internal class NotSortableStrategy<T> : SortStrategy<T> {
    override fun sort(data: List<T>, ascending: Boolean): List<T> = data
    override fun isSortable(): Boolean = false
}
