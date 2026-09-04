package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The edge case the whole "atur manual" page exists for: the same item bought in different
 * amounts. Raw prices are allowed to look like a rise; the unit price must not.
 */
class BuildPriceTrendTest {

    private val buildPriceTrend = BuildPriceTrend(FindItemCategory())

    @Test
    fun `raw basis plots what each trip cost - inflation and all`() {
        val data = buildPriceTrend(
            sessions = sessions,
            name = "Beras",
            basis = PriceBasis.RAW,
            requestedBaseUnit = null,
            overrides = emptyList(),
        )

        assertEquals(listOf(10_000, 35_000, 15_300), data.points.map { it.price })
        assertEquals(10_000, data.firstValue)
        assertEquals(15_300, data.lastValue)
        // Rice got cheaper per kilo across these three trips. The raw basis still reads as a rise,
        // because the last trip bought 900g against the first trip's 500g. That is the whole bug.
        assertTrue(data.isUp)
    }

    @Test
    fun `per-unit basis divides by the quantity - the amount bought stops mattering`() {
        val data = buildPriceTrend(
            sessions = sessions,
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = emptyList(),
        )

        // 10.000 / 0,5kg = 20.000; 35.000 / 2kg = 17.500; 15.300 / 0,9kg = 17.000.
        assertEquals(listOf(20_000, 17_500, 17_000), data.points.map { it.price })
        assertTrue(data.isDown, "the same trips read as a fall once the amount is divided out")
        assertEquals(17_000, data.cheapest)
        assertEquals(20_000, data.dearest)
    }

    @Test
    fun `a gram purchase converts into the kilo the trend is quoted in`() {
        val data = buildPriceTrend(
            sessions = sessions,
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = emptyList(),
        )
        val grams = data.purchases.first { it.itemId == "i3" }

        assertEquals(17_000, grams.value)
        assertTrue(grams.isUsable)
    }

    @Test
    fun `a purchase with no quantity is skipped rather than guessed at`() {
        val data = buildPriceTrend(
            sessions = sessions + sessionOf("s4", 4_000L, ShoppingItem("i4", "Beras", 40_000)),
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = emptyList(),
        )
        val unmeasured = data.purchases.first { it.itemId == "i4" }

        assertFalse(unmeasured.isUsable)
        assertEquals(1, data.skippedCount)
        assertEquals(3, data.usableCount)
        assertEquals(4, data.purchases.size, "it stays in the list — it is what the user must fix")
    }

    @Test
    fun `a manual quantity brings a skipped purchase back into the line`() {
        val withNoQty = sessions + sessionOf("s4", 4_000L, ShoppingItem("i4", "Beras", 40_000))
        val data = buildPriceTrend(
            sessions = withNoQty,
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = listOf(QtyOverride(itemId = "i4", nameKey = "beras", qty = 2.0, unit = "kg")),
        )
        val fixed = data.purchases.first { it.itemId == "i4" }

        assertTrue(fixed.isUsable)
        assertTrue(fixed.isOverridden)
        assertEquals(20_000, fixed.value)
        assertEquals(0, data.skippedCount)
        assertEquals(4, data.usableCount)
    }

    @Test
    fun `the override never rewrites what the receipt recorded`() {
        val data = buildPriceTrend(
            sessions = sessions,
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = listOf(QtyOverride(itemId = "i1", nameKey = "beras", qty = 1.0, unit = "kg")),
        )
        val corrected = data.purchases.first { it.itemId == "i1" }

        assertEquals(0.5, corrected.recordedQty)
        assertEquals(1.0, corrected.effectiveQty)
        assertEquals(10_000, corrected.price, "the receipt total is untouched")
    }

    @Test
    fun `a unit that cannot convert is skipped instead of being treated as the base unit`() {
        val mixed = listOf(
            sessionOf("s1", 1_000L, ShoppingItem("i1", "Beras", 10_000, qty = 1.0, unit = "kg")),
            sessionOf("s2", 2_000L, ShoppingItem("i2", "Beras", 12_000, qty = 1.0, unit = "bungkus")),
        )
        val data = buildPriceTrend(
            sessions = mixed,
            name = "Beras",
            basis = PriceBasis.PER_UNIT,
            requestedBaseUnit = "kg",
            overrides = emptyList(),
        )

        assertEquals(1, data.usableCount)
        assertEquals(1, data.skippedCount)
        assertFalse(data.hasTrend, "one measurable point is a dot, not a trend")
    }

    @Test
    fun `an item nobody bought yields nothing rather than throwing`() {
        val data = buildPriceTrend(
            sessions = sessions,
            name = "Kopi",
            basis = PriceBasis.RAW,
            requestedBaseUnit = null,
            overrides = emptyList(),
        )

        assertEquals(0, data.usableCount)
        assertFalse(data.hasTrend)
        assertTrue(data.purchases.isEmpty())
    }

    /** 0,5 kg, then 2 kg, then 900 gram — the same rice, three different amounts. */
    private val sessions = listOf(
        sessionOf("s1", 1_000L, ShoppingItem("i1", "Beras", 10_000, qty = 0.5, unit = "kg")),
        sessionOf("s2", 2_000L, ShoppingItem("i2", "Beras", 35_000, qty = 2.0, unit = "kg")),
        sessionOf("s3", 3_000L, ShoppingItem("i3", "beras ", 15_300, qty = 900.0, unit = "gram")),
    )

    private fun sessionOf(id: String, endedAt: Long, vararg items: ShoppingItem): ShoppingSession =
        ShoppingSession(
            id = id,
            store = "Superindo",
            startedAt = endedAt - 100L,
            endedAt = endedAt,
            items = items.toList(),
        )
}
