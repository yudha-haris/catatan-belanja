package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.catalog.UnitConversion
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate
import kotlin.math.roundToInt

private const val DAY_MILLIS = 86_400_000.0

/** Under half a day apart, two readings describe one sitting rather than a habit. */
private const val MIN_WINDOW_DAYS = 0.5

/** A four-month gap says the user stopped looking, not that consumption crawled. */
private const val MAX_WINDOW_DAYS = 120.0

/** Older than this and the household has probably changed; so has the rate. */
private const val LOOKBACK_DAYS = 180.0

/** Only the newest few windows count, so a habit that changed shows up within weeks. */
private const val MAX_WINDOWS = 8

/**
 * Works out how fast an item is used up, from the trail of readings it has left behind.
 *
 * Each consecutive pair of readings where the quantity fell is one consumption window. Pairs
 * where it rose are restocks and are skipped — they say nothing about consumption, they only mark
 * where the next window starts. The windows are then pooled rather than averaged one by one, so a
 * six-week observation counts for six times what a one-week observation does.
 */
class EstimateStockRate {

    operator fun invoke(
        item: StockItem,
        readings: List<StockReading>,
        nowMillis: Long,
    ): StockRateEstimate? {
        val usable = readings
            .filter { nowMillis - it.at <= LOOKBACK_DAYS * DAY_MILLIS }
            .mapNotNull { reading -> reading.inUnitOf(item.unit) }
            .sortedBy { it.at }
        if (usable.size < 2) return null

        val windows = usable.zipWithNext(::windowBetween).filterNotNull().takeLast(MAX_WINDOWS)
        if (windows.isEmpty()) return null

        val totalDays = windows.sumOf { it.days }
        if (totalDays <= 0.0) return null

        return StockRateEstimate(
            perDayQty = windows.sumOf { it.used } / totalDays,
            unit = item.unit,
            confidence = confidenceOf(windows.size),
            windowCount = windows.size,
            observedDays = totalDays.roundToInt(),
        )
    }

    /**
     * A window, or null when the pair cannot describe consumption: too close together, too far
     * apart, or a restock. A flat pair is kept — a month where nothing was used is real evidence
     * that the item is used slowly, and dropping it would bias every rate upwards.
     */
    private fun windowBetween(from: Reading, to: Reading): Window? {
        val days = (to.at - from.at) / DAY_MILLIS
        if (days < MIN_WINDOW_DAYS) return null
        if (days > MAX_WINDOW_DAYS) return null
        if (to.qty > from.qty) return null
        return Window(used = from.qty - to.qty, days = days)
    }

    private fun confidenceOf(windowCount: Int): RateConfidence {
        if (windowCount >= 3) return RateConfidence.HIGH
        if (windowCount == 2) return RateConfidence.MEDIUM
        return RateConfidence.LOW
    }

    /**
     * A reading taken in another unit still counts, as long as the two are the same kind of thing:
     * 1 kg and 50 gram belong on one line, 1 botol and 1 liter do not.
     */
    private fun StockReading.inUnitOf(unit: String): Reading? {
        val converted = UnitConversion.convert(qty = qty, from = this.unit, to = unit) ?: return null
        return Reading(qty = converted, at = at)
    }

    private data class Reading(val qty: Double, val at: Long)

    private data class Window(val used: Double, val days: Double)
}
