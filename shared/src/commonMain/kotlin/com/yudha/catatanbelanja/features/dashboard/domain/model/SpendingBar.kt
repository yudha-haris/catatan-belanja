package com.yudha.catatanbelanja.features.dashboard.domain.model

/** One bar of the "8 belanja terakhir" chart. [ratio] is 0..1 against the tallest of the eight. */
data class SpendingBar(
    val sessionId: String,
    val endedAt: Long,
    val total: Int,
    val ratio: Float,
    val isHighest: Boolean,
)
