package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.catalog.UnitConversion
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPoint
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPurchase
import kotlin.math.roundToInt

private const val MIN_TREND_POINTS = 2

/**
 * One item's price history, measured the way the user asked for.
 *
 * The default is [PriceBasis.RAW] — what the item cost that trip — because that is the number the
 * user recognises, and because most trips buy the same amount of the same thing. It is also wrong
 * the moment the amount changes: 0.5 kg one month against 2 kg the next reads as a 300% price
 * rise. [PriceBasis.PER_UNIT] fixes that by dividing by the quantity, and it is opt-in per item,
 * because it needs a quantity on every purchase and the receipt does not always carry one.
 *
 * A purchase this basis cannot measure is kept in [PriceTrendData.purchases] and left out of
 * [PriceTrendData.points]: it is the thing the adjust page exists to let the user fill in, so it
 * has to stay visible rather than silently distort the line or silently vanish from it.
 */
class BuildPriceTrend(
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(
        sessions: List<ShoppingSession>,
        name: String,
        basis: PriceBasis,
        requestedBaseUnit: String?,
        overrides: List<QtyOverride>,
    ): PriceTrendData {
        if (name.isBlank()) return PriceTrendData()

        val key = name.normalized()
        val overrideByItemId = overrides.associateBy { it.itemId }
        val samples = samples(sessions, key, overrideByItemId)
        if (samples.isEmpty()) return PriceTrendData(name = name, emoji = findItemCategory.emojiFor(name))

        val unitOptions = unitOptions(samples, name)
        val baseUnit = resolveBaseUnit(unitOptions, requestedBaseUnit)
        val measured = samples.map { it.measure(basis, baseUnit) }
        val usable = measured.filter { it.purchase.isUsable }
        val values = usable.map { it.value }

        return PriceTrendData(
            name = name,
            emoji = findItemCategory.emojiFor(name),
            basis = basis,
            baseUnit = baseUnit,
            baseUnitOptions = unitOptions,
            canUsePerUnit = unitOptions.isNotEmpty(),
            points = points(usable),
            purchases = withDeltas(measured).asReversed(),
            hasTrend = usable.size >= MIN_TREND_POINTS,
            usableCount = usable.size,
            skippedCount = measured.size - usable.size,
            firstValue = values.firstOrNull() ?: 0,
            lastValue = values.lastOrNull() ?: 0,
            deltaPercent = percentChange(values.lastOrNull() ?: 0, values.firstOrNull() ?: 0),
            isUp = values.size >= MIN_TREND_POINTS && values.last() > values.first(),
            isDown = values.size >= MIN_TREND_POINTS && values.last() < values.first(),
            cheapest = values.minOrNull() ?: 0,
            dearest = values.maxOrNull() ?: 0,
            average = averageOf(values.sum(), values.size),
        )
    }

    /** Every purchase of [key], oldest first — the order the chart draws and the deltas read in. */
    private fun samples(
        sessions: List<ShoppingSession>,
        key: String,
        overrideByItemId: Map<String, QtyOverride>,
    ): List<Sample> = sessions
        .sortedBy { it.endedMillis() }
        .flatMap { session ->
            session.items
                .filter { it.name.normalized() == key }
                .map { item ->
                    val override = overrideByItemId[item.id]
                    Sample(
                        itemId = item.id,
                        sessionId = session.id,
                        endedAt = session.endedMillis(),
                        store = session.store,
                        price = item.price,
                        recordedQty = item.qty,
                        recordedUnit = item.unit,
                        overrideQty = override?.qty,
                        overrideUnit = override?.unit,
                    )
                }
        }

    /**
     * The units this item can be quoted in: everything it was actually bought in, plus whatever
     * those convert into. The catalog's default for the name seeds the list when no trip recorded
     * a unit at all, so "per satuan" is still offered — the user only has to supply the numbers.
     */
    private fun unitOptions(samples: List<Sample>, name: String): List<String> {
        val recorded = samples
            .mapNotNull { it.effectiveUnit()?.normalized() }
            .filter { it.isNotBlank() }
            .distinct()
        val seed = recorded.ifEmpty { listOfNotNull(CatalogData.defaultUnits[name.normalized()]) }
        return seed.flatMap { UnitConversion.family(it) }.distinct()
    }

    private fun resolveBaseUnit(options: List<String>, requested: String?): String {
        val key = requested?.normalized().orEmpty()
        return options.firstOrNull { it == key } ?: options.firstOrNull().orEmpty()
    }

    private fun points(usable: List<Measured>): List<TrendPoint> {
        if (usable.size < MIN_TREND_POINTS) return emptyList()
        val cheapest = usable.minOf { it.value }
        val dearest = usable.maxOf { it.value }
        // A flat series would divide by zero; pin it to the baseline like the summary card does.
        val span = (dearest - cheapest).takeIf { it > 0 } ?: 1
        return usable.map { measured ->
            TrendPoint(
                endedAt = measured.purchase.endedAt,
                price = measured.value,
                ratio = (measured.value - cheapest).toFloat() / span.toFloat(),
            )
        }
    }

    /**
     * Fills each usable purchase's delta against the previous *usable* one. Skipping the unusable
     * ones matters: a purchase with no quantity is not a price of zero, and a delta drawn against
     * it would be pure noise.
     */
    private fun withDeltas(measured: List<Measured>): List<TrendPurchase> {
        var previous: Int? = null
        return measured.map { current ->
            if (!current.purchase.isUsable) return@map current.purchase

            val last = previous
            previous = current.value
            if (last == null) return@map current.purchase

            current.purchase.copy(
                deltaAmount = current.value - last,
                hasDelta = true,
                isUp = current.value > last,
                isDown = current.value < last,
            )
        }
    }

    /** One purchase before the basis is applied to it. */
    private data class Sample(
        val itemId: String,
        val sessionId: String,
        val endedAt: Long,
        val store: String,
        val price: Int,
        val recordedQty: Double?,
        val recordedUnit: String?,
        val overrideQty: Double?,
        val overrideUnit: String?,
    ) {
        fun effectiveQty(): Double? = overrideQty ?: recordedQty

        fun effectiveUnit(): String? = overrideUnit ?: recordedUnit

        fun measure(basis: PriceBasis, baseUnit: String): Measured = when (basis) {
            PriceBasis.RAW -> Measured(purchase = toPurchase(price, isUsable = true), value = price)
            PriceBasis.PER_UNIT -> measurePerUnit(baseUnit)
        }

        private fun measurePerUnit(baseUnit: String): Measured {
            val qty = effectiveQty()
            val unit = effectiveUnit()
            if (qty == null || qty <= 0.0 || unit.isNullOrBlank() || baseUnit.isBlank()) {
                return Measured(purchase = toPurchase(0, isUsable = false), value = 0)
            }

            val converted = UnitConversion.convert(qty, unit, baseUnit)
            if (converted == null || converted <= 0.0) {
                return Measured(purchase = toPurchase(0, isUsable = false), value = 0)
            }

            val unitPrice = (price.toDouble() / converted).roundToInt()
            return Measured(purchase = toPurchase(unitPrice, isUsable = true), value = unitPrice)
        }

        private fun toPurchase(value: Int, isUsable: Boolean): TrendPurchase = TrendPurchase(
            itemId = itemId,
            sessionId = sessionId,
            endedAt = endedAt,
            store = store,
            price = price,
            recordedQty = recordedQty,
            recordedUnit = recordedUnit,
            effectiveQty = effectiveQty(),
            effectiveUnit = effectiveUnit(),
            isOverridden = overrideQty != null,
            value = value,
            isUsable = isUsable,
            deltaAmount = 0,
            hasDelta = false,
            isUp = false,
            isDown = false,
        )
    }

    private data class Measured(val purchase: TrendPurchase, val value: Int)
}
