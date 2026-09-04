package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One row of the spending ranking. [ratio] is 0..1 against the top item, [sharePercent] is the
 * item's cut of everything spent inside the selected scope.
 */
data class TopItem(
    val name: String,
    val emoji: String,
    val total: Int,
    val purchaseCount: Int,
    val ratio: Float,
    val sharePercent: Int,
)
