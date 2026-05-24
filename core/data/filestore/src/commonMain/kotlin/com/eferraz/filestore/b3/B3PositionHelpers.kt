package com.eferraz.filestore.b3

import com.eferraz.filestore.b3.dto.B3Position

internal fun String.isEmptyCell(): Boolean {
    val trimmed = trim()
    return trimmed.isEmpty() || trimmed == "-"
}

internal fun isBlankRow(vararg cells: String): Boolean =
    cells.all { it.isEmptyCell() }

/** Mantém posições até (sem incluir) a primeira linha em branco. */
internal fun <T : B3Position> Iterable<T>.dropAfterBlankRow(): List<T> =
    takeWhile { !it.isBlankRow() }
