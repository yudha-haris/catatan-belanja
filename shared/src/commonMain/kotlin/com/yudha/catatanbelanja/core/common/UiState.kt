package com.yudha.catatanbelanja.core.common

sealed interface UiState<out T> {
    data object Initial : UiState<Nothing>

    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Error(val failure: Failure) : UiState<Nothing>
}
