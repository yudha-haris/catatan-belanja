package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow
import kotlin.math.abs
import kotlin.math.roundToInt

/** Below this the guess was not close enough to be worth mentioning; saying so every time is nagging. */
private const val HIT_THRESHOLD = 0.85

/**
 * Marks the app's own homework. When the user writes down what is really left, the estimate that
 * was on screen a moment earlier becomes a testable prediction — so it gets tested, and the score
 * is handed straight back to the user.
 *
 * This is the only place the feature ever claims to have been right, and it can only do so
 * against a number the user typed themselves.
 */
class ScoreStockEstimate {

    /** The accuracy percentage when the estimate landed close, or null when there is nothing to say. */
    operator fun invoke(shadow: StockShadow?, previous: StockItem?, saved: StockItem): Int? {
        if (shadow == null || previous == null) return null
        // Across a unit change the two numbers are not measuring the same thing.
        if (previous.unit != saved.unit) return null
        // An untouched quantity is not a fresh observation, so there is nothing to score against.
        if (previous.qty == saved.qty) return null
        if (previous.qty <= 0.0) return null

        val accuracy = 1.0 - abs(saved.qty - shadow.estimatedQty) / previous.qty
        if (accuracy < HIT_THRESHOLD) return null
        return (accuracy * 100).roundToInt().coerceAtMost(100)
    }
}
