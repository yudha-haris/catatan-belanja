package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.catalog.UnitConversion
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence
import com.yudha.catatanbelanja.features.stock.domain.model.StockRateEstimate
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow
import kotlin.math.roundToInt

private const val DAY_MILLIS = 86_400_000.0
private const val CENTS = 100.0

/** Under a day old, the stored quantity is still the best answer anyone has. */
private const val MIN_SHADOW_DAYS = 1.0

/**
 * Below this much of the shelf used up, the estimate would round to the number already on screen
 * and the only thing it would add is doubt.
 */
private const val MIN_SHADOW_FRACTION = 0.05

/** "Running out soon" in the sense a shopper cares about: before the next weekly trip. */
private const val SOON_DAYS = 7

/**
 * Projects a stock item forward from the last time its quantity was actually known, at whichever
 * rate applies — the one the user stated, or failing that the one inferred from its history.
 *
 * Returns null wherever the honest answer is silence: the user switched the estimate off, the
 * shelf is already empty, no rate could be found, or too little has changed to be worth a word.
 */
class EstimateCurrentStock {

    /**
     * [scale] is the level bar's denominator, owned by the caller so the ghost mark and the real
     * fill are drawn against exactly the same axis.
     */
    operator fun invoke(
        item: StockItem,
        rate: StockRate,
        auto: StockRateEstimate?,
        scale: Double,
        nowMillis: Long,
    ): StockShadow? {
        if (rate.mode == RateMode.OFF) return null
        if (item.qty <= 0.0) return null

        val perDay = perDayOf(item, rate, auto) ?: return null
        if (perDay <= 0.0) return null

        val daysSince = (nowMillis - item.updatedAt) / DAY_MILLIS
        if (daysSince < MIN_SHADOW_DAYS) return null

        val used = perDay * daysSince
        if (used < item.qty * MIN_SHADOW_FRACTION) return null

        val estimated = (item.qty - used).coerceAtLeast(0.0).roundedToCents()
        return StockShadow(
            estimatedQty = estimated,
            ratio = (estimated / scale).coerceIn(0.0, 1.0).toFloat(),
            perDayQty = perDay,
            unit = item.unit,
            confidence = confidenceOf(rate, auto),
            windowCount = if (rate.mode == RateMode.MANUAL) 0 else auto?.windowCount ?: 0,
            daysSinceUpdate = daysSince.roundToInt(),
            daysLeft = daysLeftOf(estimated, perDay),
            isBelowMin = item.minQty?.let { estimated <= it } ?: (estimated <= 0.0),
            isEmpty = estimated <= 0.0,
        )
    }

    /** True once the estimate says the shelf empties within the week. */
    fun isRunningOutSoon(shadow: StockShadow): Boolean {
        val daysLeft = shadow.daysLeft ?: return true
        return daysLeft <= SOON_DAYS
    }

    /**
     * A stated rate wins outright — the user knows their own household better than a trend line
     * does, and quietly overruling them is how a feature stops being trusted.
     */
    private fun perDayOf(item: StockItem, rate: StockRate, auto: StockRateEstimate?): Double? {
        if (rate.mode == RateMode.MANUAL) return manualPerDay(item, rate)
        return auto?.perDayQty
    }

    private fun manualPerDay(item: StockItem, rate: StockRate): Double? {
        val qty = rate.manualQty ?: return null
        val converted = UnitConversion.convert(
            qty = qty,
            from = rate.manualUnit ?: item.unit,
            to = item.unit,
        ) ?: return null
        return converted / rate.manualPeriod.days
    }

    private fun confidenceOf(rate: StockRate, auto: StockRateEstimate?): RateConfidence {
        if (rate.mode == RateMode.MANUAL) return RateConfidence.EXACT
        return auto?.confidence ?: RateConfidence.LOW
    }

    private fun daysLeftOf(estimated: Double, perDay: Double): Int? {
        if (estimated <= 0.0) return null
        return (estimated / perDay).roundToInt()
    }

    /** Quantities are shown to one or two decimals; carrying more only invents precision. */
    private fun Double.roundedToCents(): Double = (this * CENTS).roundToInt() / CENTS
}
