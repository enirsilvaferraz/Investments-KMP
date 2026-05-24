package com.eferraz.filestore.b3.dto

internal interface B3Position {
    val produto: String
}

internal fun <T : B3Position> Iterable<T>.filterByProduto(): List<T> =
    filter { position ->
        val produto = position.produto.trim()
        !produto.startsWith("Total", ignoreCase = true) &&
            !produto.startsWith("Subtotal", ignoreCase = true)
    }
