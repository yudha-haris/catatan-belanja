package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One purchase of the tracked item, as the adjust list shows it.
 *
 * [recordedQty] is what the trip actually logged and is never rewritten; [effectiveQty] is what
 * the trend used, which is the override when there is one. A purchase the current basis cannot
 * measure — no quantity anywhere, or one in a unit that does not convert — is [isUsable] false and
 * is left out of the chart rather than guessed at.
 */
data class TrendPurchase(
    val itemId: String,
    val sessionId: String,
    val endedAt: Long,
    val store: String,
    val price: Int,
    val recordedQty: Double?,
    val recordedUnit: String?,
    val effectiveQty: Double?,
    val effectiveUnit: String?,
    val isOverridden: Boolean,
    val value: Int,
    val isUsable: Boolean,
    val deltaAmount: Int,
    val hasDelta: Boolean,
    val isUp: Boolean,
    val isDown: Boolean,
)
