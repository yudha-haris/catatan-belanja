package com.yudha.catatanbelanja.core.common

/** Developer-facing error description. UI shows a localized message and uses this as detail. */
data class Failure(
    val message: String,
    val code: String? = null,
    val cause: Throwable? = null,
)
