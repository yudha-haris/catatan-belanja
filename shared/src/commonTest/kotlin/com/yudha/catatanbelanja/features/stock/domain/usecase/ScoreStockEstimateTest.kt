package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScoreStockEstimateTest {

    private val score = ScoreStockEstimate()

    private fun item(qty: Double, unit: String = "kg") =
        StockItem(id = "sugar", name = "Gula", qty = qty, unit = unit, updatedAt = 0L)

    private fun shadow(estimatedQty: Double) = StockShadow(
        estimatedQty = estimatedQty,
        ratio = 0.4f,
        perDayQty = 0.02,
        unit = "kg",
        confidence = RateConfidence.HIGH,
        windowCount = 4,
        daysSinceUpdate = 20,
        daysLeft = 20,
        isBelowMin = false,
        isEmpty = false,
    )

    @Test
    fun `scores a close guess`() {
        val result = score(
            shadow = shadow(estimatedQty = 0.6),
            previous = item(qty = 1.0),
            saved = item(qty = 0.55),
        )

        assertEquals(95, result)
    }

    @Test
    fun `stays quiet when the guess was well off`() {
        val result = score(
            shadow = shadow(estimatedQty = 0.6),
            previous = item(qty = 1.0),
            saved = item(qty = 0.2),
        )

        assertNull(result)
    }

    @Test
    fun `stays quiet when the quantity was not actually changed`() {
        val result = score(
            shadow = shadow(estimatedQty = 1.0),
            previous = item(qty = 1.0),
            saved = item(qty = 1.0),
        )

        assertNull(result)
    }

    @Test
    fun `stays quiet across a unit change where the two numbers measure different things`() {
        val result = score(
            shadow = shadow(estimatedQty = 0.6),
            previous = item(qty = 1.0, unit = "kg"),
            saved = item(qty = 0.9, unit = "gram"),
        )

        assertNull(result)
    }

    @Test
    fun `stays quiet when there was no estimate on screen to score`() {
        assertNull(score(shadow = null, previous = item(qty = 1.0), saved = item(qty = 0.5)))
    }
}
