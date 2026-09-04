package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.ReadingSource
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val DAY = 86_400_000L
private const val NOW = 400L * DAY
private const val TOLERANCE = 0.0001

class EstimateStockRateTest {

    private val estimate = EstimateStockRate()

    private fun item(unit: String = "kg") = StockItem(
        id = "sugar",
        name = "Gula",
        qty = 1.0,
        unit = unit,
        updatedAt = NOW,
    )

    private fun reading(
        qty: Double,
        daysAgo: Long,
        unit: String = "kg",
        source: ReadingSource = ReadingSource.MANUAL,
    ) = StockReading(
        itemId = "sugar",
        qty = qty,
        unit = unit,
        at = NOW - daysAgo * DAY,
        source = source,
    )

    @Test
    fun `derives a daily rate from two readings a month apart`() {
        val result = estimate(
            item = item(),
            readings = listOf(reading(1.0, daysAgo = 60), reading(0.4, daysAgo = 30)),
            nowMillis = NOW,
        )

        assertEquals(0.02, result?.perDayQty ?: 0.0, TOLERANCE)
        assertEquals("kg", result?.unit)
        assertEquals(1, result?.windowCount)
        assertEquals(30, result?.observedDays)
    }

    /** 1 kg of sugar bought on the 1st, 50 gram left on the 30th — the case the feature exists for. */
    @Test
    fun `reads a later reading logged in a smaller unit of the same family`() {
        val result = estimate(
            item = item(unit = "kg"),
            readings = listOf(
                reading(1.0, daysAgo = 59, unit = "kg", source = ReadingSource.PURCHASE),
                reading(50.0, daysAgo = 30, unit = "gram", source = ReadingSource.CHECK),
            ),
            nowMillis = NOW,
        )

        // 0.95 kg gone over 29 days.
        assertEquals(0.95 / 29.0, result?.perDayQty ?: 0.0, TOLERANCE)
    }

    @Test
    fun `ignores a restock instead of reading it as negative consumption`() {
        val result = estimate(
            item = item(),
            readings = listOf(
                reading(1.0, daysAgo = 90),
                reading(0.1, daysAgo = 60),
                // Bought more: the quantity jumps, which is not a consumption window.
                reading(2.1, daysAgo = 59, source = ReadingSource.PURCHASE),
                reading(1.5, daysAgo = 29),
            ),
            nowMillis = NOW,
        )

        // Only the two genuine drops count: 0.9 over 30 days, then 0.6 over 30.
        assertEquals(1.5 / 60.0, result?.perDayQty ?: 0.0, TOLERANCE)
        assertEquals(2, result?.windowCount)
        assertEquals(RateConfidence.MEDIUM, result?.confidence)
    }

    @Test
    fun `skips a pair taken minutes apart`() {
        val result = estimate(
            item = item(),
            readings = listOf(
                StockReading("sugar", 1.0, "kg", NOW - DAY, ReadingSource.MANUAL),
                StockReading("sugar", 0.4, "kg", NOW - DAY + 60_000L, ReadingSource.MANUAL),
            ),
            nowMillis = NOW,
        )

        assertNull(result)
    }

    @Test
    fun `skips a gap too long to describe a habit`() {
        val result = estimate(
            item = item(),
            readings = listOf(reading(1.0, daysAgo = 175), reading(0.2, daysAgo = 10)),
            nowMillis = NOW,
        )

        assertNull(result)
    }

    @Test
    fun `ignores a reading whose unit cannot be compared`() {
        val result = estimate(
            item = item(unit = "kg"),
            readings = listOf(
                reading(1.0, daysAgo = 60, unit = "kg"),
                reading(2.0, daysAgo = 30, unit = "botol"),
            ),
            nowMillis = NOW,
        )

        assertNull(result)
    }

    @Test
    fun `keeps a flat window rather than biasing the rate upwards`() {
        val result = estimate(
            item = item(),
            readings = listOf(
                reading(1.0, daysAgo = 90),
                // Nothing used for a month: real evidence that this item moves slowly.
                reading(1.0, daysAgo = 60),
                reading(0.4, daysAgo = 30),
            ),
            nowMillis = NOW,
        )

        assertEquals(0.6 / 60.0, result?.perDayQty ?: 0.0, TOLERANCE)
        assertEquals(2, result?.windowCount)
    }

    @Test
    fun `grows more confident as windows accumulate`() {
        val readings = mutableListOf(reading(3.0, daysAgo = 120))
        var qty = 3.0
        var daysAgo = 100L
        val confidences = mutableListOf<RateConfidence>()
        repeat(3) {
            qty -= 0.5
            readings += reading(qty, daysAgo = daysAgo)
            daysAgo -= 20
            confidences += estimate(item(), readings, NOW)?.confidence ?: RateConfidence.LOW
        }

        assertEquals(listOf(RateConfidence.LOW, RateConfidence.MEDIUM, RateConfidence.HIGH), confidences)
    }

    @Test
    fun `says nothing when there is only one reading`() {
        assertNull(estimate(item(), listOf(reading(1.0, daysAgo = 30)), NOW))
    }

    @Test
    fun `only the most recent windows count so a changed habit shows up`() {
        // Ten readings a month apart at a trickle, then eight at ten times the pace. The cap on
        // how many windows are pooled means only the fast stretch survives into the answer.
        val readings = mutableListOf<StockReading>()
        (0 until 10).forEach { index ->
            readings += reading(100.0 - index, daysAgo = 170 - index * 10L)
        }
        (0 until 8).forEach { index ->
            readings += reading(81.0 - index * 10, daysAgo = 70 - index * 10L)
        }

        val result = estimate(item(), readings, NOW)

        assertEquals(1.0, result?.perDayQty ?: 0.0, TOLERANCE)
        assertEquals(8, result?.windowCount)
    }
}
