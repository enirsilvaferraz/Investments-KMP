package com.eferraz.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public abstract class AppUseCase<in P, out R>(private val context: CoroutineDispatcher = Dispatchers.Default) {

    public suspend operator fun invoke(param: P): Result<R> = withContext(context) {
        result { execute(param) }
    }

    protected abstract suspend fun execute(param: P): R

    private suspend inline fun <T> result(function: suspend () -> T): Result<T> =
        runCatching {
            Result.success(function())
        }.getOrElse {
            Result.failure(it)
        }
}