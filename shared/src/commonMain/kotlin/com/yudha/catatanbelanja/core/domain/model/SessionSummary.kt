package com.yudha.catatanbelanja.core.domain.model

/** Shared view model: a session plus everything the UI shows about it. */
data class SessionSummary(
    val session: ShoppingSession,
    val total: Int,
    val itemCount: Int,
)
