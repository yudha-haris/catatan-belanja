package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One trip in the spending report's list. [deltaAmount] is signed against the previous trip.
 *
 * [hasName] and [hasStore] exist so the row composable picks a string resource instead of running
 * a `name.ifBlank { store }` ladder of its own — the same job `HistorySessionRowView.showStore` does.
 */
data class TripSpending(
    val sessionId: String,
    val name: String,
    val store: String,
    val hasName: Boolean,
    val hasStore: Boolean,
    val endedAt: Long,
    val total: Int,
    val itemCount: Int,
    val deltaAmount: Int,
    val hasDelta: Boolean,
    val isUp: Boolean,
    val isDown: Boolean,
)
