package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.RatePeriod
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val DAY = 86_400_000L
private const val NOW = 400L * DAY
private const val TOLERANCE = 0.011

class EstimateCurrentStockTest {

    private val estimate = EstimateCurrentStock()

    private fun item(
        qty: Double = 1.0,
        unit: String = "kg",
        minQty: Double? = null,
        daysAgo: Long = 20,
    ) = StockItem(
        id = "sugar",
        name = "Gula",
        qty = qty,
        unit = unit,
        minQty = minQty,
        fullQty = 1.0,
        updatedAt = NOW - daysAgo * DAY,
    )

    private fun auto(perDay: Double, unit: String = "kg") = StockRateEstimate(
        perDayQty = perDay,
        unit = unit,
        confidence = RateConfidence.HIGH,
        windowCount = 4,
        observedDays = 90,
    )

    @Test
    fun `counts the stored quantity down at the inferred rate`() {
        val shadow = estimate(
            item = item(qty = 1.0, daysAgo = 20),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        // 1 kg less 20 days at 0.02 kg/day.
        assertEquals(0.6, shadow?.estimatedQty ?: 0.0, TOLERANCE)
        assertEquals(20, shadow?.daysSinceUpdate)
        assertEquals(30, shadow?.daysLeft)
        assertEquals(RateConfidence.HIGH, shadow?.confidence)
    }

    @Test
    fun `a stated rate overrules the inferred one`() {
        val shadow = estimate(
            item = item(qty = 1.0, daysAgo = 30),
            rate = StockRate(
                itemId = "sugar",
                mode = RateMode.MANUAL,
                manualQty = 300.0,
                manualUnit = "gram",
                manualPeriod = RatePeriod.WEEK,
            ),
            // Wildly different, and must be ignored entirely.
            auto = auto(perDay = 0.5),
            scale = 1.0,
            nowMillis = NOW,
        )

        // 300 g a week is ~0.0428 kg/day; over 30 days that is ~1.29 kg, so the shelf reads empty.
        assertEquals(0.0, shadow?.estimatedQty ?: -1.0, TOLERANCE)
        assertEquals(RateConfidence.EXACT, shadow?.confidence)
        assertTrue(shadow?.isEmpty == true)
        assertNull(shadow?.daysLeft)
    }

    @Test
    fun `says nothing when the user switched the estimate off`() {
        val shadow = estimate(
            item = item(daysAgo = 60),
            rate = StockRate(itemId = "sugar", mode = RateMode.OFF),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `says nothing when the stored quantity is still fresh`() {
        val shadow = estimate(
            item = item(daysAgo = 0),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `says nothing when it would only repeat the number already on screen`() {
        // Two days at a trickle: under a twentieth of the shelf, so there is nothing to add.
        val shadow = estimate(
            item = item(qty = 1.0, daysAgo = 2),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.005),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `says nothing about an already empty shelf`() {
        val shadow = estimate(
            item = item(qty = 0.0, daysAgo = 30),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `says nothing when no rate could be found`() {
        val shadow = estimate(
            item = item(daysAgo = 30),
            rate = StockRate(itemId = "sugar"),
            auto = null,
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `flags an estimate that has crossed the reminder threshold the stored quantity has not`() {
        val shadow = estimate(
            item = item(qty = 1.0, minQty = 0.5, daysAgo = 30),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertEquals(0.4, shadow?.estimatedQty ?: 0.0, TOLERANCE)
        assertTrue(shadow?.isBelowMin == true)
        assertFalse(shadow?.isEmpty == true)
    }

    @Test
    fun `drops a manual rate quoted in a unit that cannot be converted`() {
        val shadow = estimate(
            item = item(unit = "kg", daysAgo = 30),
            rate = StockRate(
                itemId = "sugar",
                mode = RateMode.MANUAL,
                manualQty = 1.0,
                manualUnit = "botol",
                manualPeriod = RatePeriod.WEEK,
            ),
            auto = auto(perDay = 0.02),
            scale = 1.0,
            nowMillis = NOW,
        )

        assertNull(shadow)
    }

    @Test
    fun `places the ghost mark on the same scale as the real fill`() {
        val shadow = estimate(
            item = item(qty = 1.0, daysAgo = 20),
            rate = StockRate(itemId = "sugar"),
            auto = auto(perDay = 0.02),
            scale = 2.0,
            nowMillis = NOW,
        )

        assertEquals(0.3f, shadow?.ratio ?: 0f, 0.01f)
    }
}
