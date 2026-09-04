package com.yudha.catatanbelanja.core.common

import kotlin.coroutines.cancellation.CancellationException

sealed interface Resource<out T> {
    data class Success<T>(val value: T) : Resource<T>

    data class Error(val failure: Failure) : Resource<Nothing>
}

val Resource<*>.isError: Boolean
    get() = this is Resource.Error

fun <T> Resource<T>.dataOrNull(): T? = (this as? Resource.Success)?.value

fun Resource<*>.failureOrNull(): Failure? = (this as? Resource.Error)?.failure

inline fun <T, R> Resource<T>.returnWhen(
    onSuccess: (T) -> R,
    onError: (Failure) -> R,
): R = when (this) {
    is Resource.Success -> onSuccess(value)
    is Resource.Error -> onError(failure)
}

/** Runs [block], wrapping any throw into Resource.Error. Used by every repository impl. */
suspend inline fun <T> resourceOf(
    message: String,
    crossinline block: suspend () -> T,
): Resource<T> = try {
    Resource.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Throwable) {
    Resource.Error(Failure(message = message, cause = error))
}
