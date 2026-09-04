package com.yudha.catatanbelanja.features.stock.domain.model

/**
 * What the app thinks is on the shelf *right now*, as opposed to [com.yudha.catatanbelanja
 * .core.domain.model.StockItem.qty], which is what somebody last wrote down. A shadow is only
 * ever a suggestion: it is drawn beside the stored quantity and never replaces it until the user
 * says so, because a number the app invented and then saved by itself would be indistinguishable
 * from one the user checked.
 *
 * Absent (null) whenever there is nothing worth saying — no rate, an empty shelf, or a stored
 * quantity so recent that the estimate would simply repeat it.
 */
data class StockShadow(
    val estimatedQty: Double,
    /** Where the ghost mark sits on the level bar, against the same scale as the real fill. */
    val ratio: Float,
    val perDayQty: Double,
    val unit: String,
    /** [RateConfidence.EXACT] means the user set this rate; anything else is inferred. */
    val confidence: RateConfidence,
    /** How many consumption windows the inferred rate rests on. Zero when the user set it. */
    val windowCount: Int,
    val daysSinceUpdate: Int,
    /** Days until the estimate reaches zero, or null once it already has. */
    val daysLeft: Int?,
    /** The estimate has crossed the item's reminder threshold, even though the stored qty has not. */
    val isBelowMin: Boolean,
    val isEmpty: Boolean,
)
