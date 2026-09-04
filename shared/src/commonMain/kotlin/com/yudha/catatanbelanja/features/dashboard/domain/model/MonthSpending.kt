package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One month of the spending report. [ratio] is 0..1 against the priciest month in the window;
 * the delta compares against the month before it *in the same series*, so a gap month (nothing
 * bought at all) reads as no comparison rather than as a 100% drop.
 */
data class MonthSpending(
    val monthKey: String,
    val total: Int,
    val sessionCount: Int,
    val average: Int,
    val ratio: Float,
    val isHighest: Boolean,
    val deltaPercent: Int,
    val hasDelta: Boolean,
    val isUp: Boolean,
    val isDown: Boolean,
)
