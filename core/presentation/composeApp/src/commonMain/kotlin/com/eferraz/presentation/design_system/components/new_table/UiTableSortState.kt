package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

internal data class UiTableSortState<T>(
    val sortedColumnIndex: Int = 0,
    val isAscending: Boolean = true,
) {

    fun sort(index: Int): UiTableSortState<T> =
        UiTableSortState(
            sortedColumnIndex = index,
            isAscending = if (sortedColumnIndex == index) !isAscending else true,
        )

    internal fun createComparator(columns: List<((T) -> Comparable<*>)?>): Comparator<T> {
        return compareBy<T> { columns[sortedColumnIndex]?.invoke(it) }
            .let { if (isAscending) it else it.reversed() }
    }
}

/**
 * Gerencia o estado e ordenação dos dados da tabela.
 * Encapsula toda a lógica de controle dos itens da lista e ordenação.
 */
internal class UiTableSortStateManager<T> {

    internal var sortState: UiTableSortState<T> by mutableStateOf(UiTableSortState())
        private set

    internal val sortedData: SnapshotStateList<T> = mutableStateListOf()

    fun sort(index: Int) {
        sortState = sortState.sort(index)
    }
}

/**
 * Cria e retorna um [UiTableSortStateManager] que será lembrado durante a composição.
 * O estado calcula automaticamente os dados ordenados baseados no estado de ordenação atual.
 */
@Composable
internal fun <T> rememberUiTableSortState(
    data: List<T>,
    columns: List<((T) -> Comparable<*>)?>,
): UiTableSortStateManager<T> {

    val manager = remember { UiTableSortStateManager<T>() }

    val calculatedSortedData = remember(data, columns) {
        derivedStateOf {
            data.sortedWith(manager.sortState.createComparator(columns)).toMutableStateList()
        }
    }

    LaunchedEffect(calculatedSortedData.value) {
        manager.sortedData.clear()
        manager.sortedData.addAll(calculatedSortedData.value)
    }

    return manager
}