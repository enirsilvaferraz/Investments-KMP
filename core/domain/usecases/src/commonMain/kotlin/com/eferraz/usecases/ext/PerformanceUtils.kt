package com.eferraz.usecases.ext

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Mede o tempo de execução de uma função não suspend e retorna o tempo em milissegundos
 * junto com o resultado da função. Imprime o resultado no console com o label fornecido.
 * 
 * @param label String identificadora para exibir no console
 * @param block Função a ser medida
 * @return Pair contendo o tempo em milissegundos e o resultado da função
 */
@OptIn(ExperimentalTime::class)
public inline fun <T> measureTimeMillis(label: String, block: () -> T): Pair<Long, T> {
    val startTime = Clock.System.now().toEpochMilliseconds()
    val result = block()
    val endTime = Clock.System.now().toEpochMilliseconds()
    val time = endTime - startTime
    println("$label: $time ms")
    return time to result
}

/**
 * Mede o tempo de execução de uma função suspend e retorna o tempo em milissegundos
 * junto com o resultado da função. Imprime o resultado no console com o label fornecido.
 * 
 * @param label String identificadora para exibir no console
 * @param block Função suspend a ser medida
 * @return Pair contendo o tempo em milissegundos e o resultado da função
 */
@OptIn(ExperimentalTime::class)
public suspend inline fun <T> measureTimeMillis(label: String, block: suspend () -> T): Pair<Long, T> {
    val startTime = Clock.System.now().toEpochMilliseconds()
    val result = block()
    val endTime = Clock.System.now().toEpochMilliseconds()
    val time = endTime - startTime
    println("$label: $time ms")
    return time to result
}

/**
 * Função auxiliar para forçar o uso da versão suspend de measureTimeMillis.
 * Usada internamente para resolver ambiguidade de sobrecarga.
 */
@PublishedApi
internal suspend inline fun <T> measureTimeMillisSuspend(label: String, block: suspend () -> T): Pair<Long, T> =
    measureTimeMillis(label, block)

