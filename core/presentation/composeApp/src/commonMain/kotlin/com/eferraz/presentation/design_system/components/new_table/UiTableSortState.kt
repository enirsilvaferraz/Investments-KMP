package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

internal data class UiTableSortState<T>(
    val data: List<T>,
    val sortedColumnIndex: Int = 0,
    val isAscending: Boolean = true,
    val columns: List<((T) -> Comparable<*>)?>,
) {

    private val sortedBy by lazy {
        compareBy<T> { columns[sortedColumnIndex]?.invoke(it) }.let { if (isAscending) it else it.reversed() }
    }

    val sortedData: SnapshotStateList<T> by lazy {
        data.sortedWith(sortedBy).toMutableStateList()
    }

    fun sort(index: Int) =
        UiTableSortState(
            data = data,
            sortedColumnIndex = index,
            isAscending = if (sortedColumnIndex == index) !isAscending else true,
            columns = columns
        )
}