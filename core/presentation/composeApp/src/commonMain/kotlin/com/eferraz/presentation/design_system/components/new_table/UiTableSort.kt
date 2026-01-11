package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

// ============================================================================
// INTERFACE DE ESTRATÉGIA DE ORDENAÇÃO
// ============================================================================

internal interface SortStrategy<T> {

    fun createComparator(
        sortedColumnIndex: Int,
        isAscending: Boolean,
        columns: List<((T) -> Comparable<*>)?>,
    ): Comparator<T>
}

// ============================================================================
// ESTADO DE ORDENAÇÃO
// ============================================================================

@Stable
internal data class SortState(
    val sortedColumnIndex: Int = 0,
    val isAscending: Boolean = true,
) {

    fun sort(index: Int) =
        SortState(
            sortedColumnIndex = index,
            isAscending = if (sortedColumnIndex == index) !isAscending else true
        )
}

@Stable
internal class SortStateManager<T> {

    var sortState: SortState by mutableStateOf(SortState())
        private set

    val sortedData: SnapshotStateList<T> = mutableStateListOf()

    fun sort(index: Int) {
        sortState = sortState.sort(index)
    }
}

@Stable
internal class SortDataCalculator<T>(
    private val strategy: SortStrategy<T>,
) {

    fun calculate(
        data: List<T>,
        sortState: SortState,
        columns: List<((T) -> Comparable<*>)?>,
    ): List<T> {

        return data.sortedWith(
            strategy.createComparator(
                sortState.sortedColumnIndex,
                sortState.isAscending,
                columns
            )
        )
    }
}

// ============================================================================
// IMPLEMENTAÇÃO PADRÃO DE ESTRATÉGIA
// ============================================================================

@Stable
internal class DefaultSortStrategy<T> : SortStrategy<T> {

    override fun createComparator(
        sortedColumnIndex: Int,
        isAscending: Boolean,
        columns: List<((T) -> Comparable<*>)?>,
    ): Comparator<T> {

        val comparator = compareBy<T> { columns[sortedColumnIndex]?.invoke(it) }
        return if (isAscending) comparator else comparator.reversed()
    }
}

// ============================================================================
// COMPOSABLE DE ESTADO
// ============================================================================

@Composable
internal fun <T> rememberSortState(
    data: List<T>,
    columns: List<((T) -> Comparable<*>)?>,
    strategy: SortStrategy<T>,
): SortStateManager<T> {

    val manager = remember { SortStateManager<T>() }
    val calculator = remember(strategy) { SortDataCalculator(strategy) }

    // PERFORMANCE: derivedStateOf observa mudanças automaticamente em manager.sortState, data e columns
    val sortedData = remember(manager.sortState, data, columns) {
        derivedStateOf {
            calculator.calculate(data, manager.sortState, columns)
        }
    }

    // PERFORMANCE: LaunchedEffect observa apenas o valor calculado
    LaunchedEffect(sortedData.value) {
        manager.sortedData.clear()
        manager.sortedData.addAll(sortedData.value)
    }

    return manager
}

