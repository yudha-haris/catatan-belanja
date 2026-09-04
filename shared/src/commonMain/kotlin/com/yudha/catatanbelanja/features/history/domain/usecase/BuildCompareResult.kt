package com.yudha.catatanbelanja.features.history.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.history.domain.model.CompareResult
import com.yudha.catatanbelanja.features.history.domain.model.CompareRow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Diffs two shopping sessions. Both sides are first aggregated by normalized name (the
 * prototype's `buildMap`), so buying beras twice in one trip compares as one line.
 */
class BuildCompareResult(private val findItemCategory: FindItemCategory) {
    operator fun invoke(sessionA: ShoppingSession, sessionB: ShoppingSession): CompareResult {
        val aggregatesA = aggregate(sessionA)
        val aggregatesB = aggregate(sessionB)

        val inBoth = aggregatesA
            .filterKeys { aggregatesB.containsKey(it) }
            .map { (key, a) -> bothRow(a, aggregatesB.getValue(key)) }
            .sortedByDescending { it.deltaAmount ?: 0 }
        val onlyInA = aggregatesA.filterKeys { !aggregatesB.containsKey(it) }.map { singleRow(it.value, isSideA = true) }
        val onlyInB = aggregatesB.filterKeys { !aggregatesA.containsKey(it) }.map { singleRow(it.value, isSideA = false) }

        val totalA = sessionA.items.sumOf { it.price }
        val totalB = sessionB.items.sumOf { it.price }

        return CompareResult(
            inBoth = inBoth,
            onlyInA = onlyInA,
            onlyInB = onlyInB,
            onlyInATotal = onlyInA.sumOf { it.priceA ?: 0 },
            onlyInBTotal = onlyInB.sumOf { it.priceB ?: 0 },
            sameCount = inBoth.size,
            differentCount = onlyInA.size + onlyInB.size,
            totalA = totalA,
            totalB = totalB,
            delta = totalB - totalA,
            deltaPercent = percentChange(from = totalA, to = totalB),
            upCount = inBoth.count { it.delta == CompareRow.Delta.UP },
            downCount = inBoth.count { it.delta == CompareRow.Delta.DOWN },
        )
    }

    private fun aggregate(session: ShoppingSession): Map<String, Aggregate> {
        val aggregates = LinkedHashMap<String, Aggregate>()
        session.items.forEach { item ->
            val key = item.name.normalized()
            val current = aggregates[key] ?: Aggregate(name = item.name, unit = item.unit)
            aggregates[key] = current.copy(
                price = current.price + item.price,
                qty = current.qty + (item.qty ?: 0.0),
            )
        }
        return aggregates
    }

    private fun bothRow(a: Aggregate, b: Aggregate): CompareRow {
        val delta = b.price - a.price
        return CompareRow(
            name = a.name,
            emoji = findItemCategory.emojiFor(a.name),
            priceA = a.price,
            qtyA = a.qty.takeIf { it > 0.0 },
            unitA = a.unit,
            priceB = b.price,
            qtyB = b.qty.takeIf { it > 0.0 },
            unitB = b.unit,
            deltaAmount = abs(delta),
            delta = directionOf(delta),
        )
    }

    private fun singleRow(aggregate: Aggregate, isSideA: Boolean): CompareRow = CompareRow(
        name = aggregate.name,
        emoji = findItemCategory.emojiFor(aggregate.name),
        priceA = aggregate.price.takeIf { isSideA },
        qtyA = aggregate.qty.takeIf { isSideA && it > 0.0 },
        unitA = aggregate.unit.takeIf { isSideA },
        priceB = aggregate.price.takeIf { !isSideA },
        qtyB = aggregate.qty.takeIf { !isSideA && it > 0.0 },
        unitB = aggregate.unit.takeIf { !isSideA },
        deltaAmount = null,
        delta = CompareRow.Delta.NONE,
    )

    private fun directionOf(delta: Int): CompareRow.Delta {
        if (delta > 0) return CompareRow.Delta.UP
        if (delta < 0) return CompareRow.Delta.DOWN
        return CompareRow.Delta.SAME
    }

    /** Mirrors the prototype's `pct()`: 0 when there is no base to grow from. */
    private fun percentChange(from: Int, to: Int): Int {
        if (from == 0) return 0
        return ((to - from).toDouble() / from * 100).roundToInt()
    }

    private data class Aggregate(
        val name: String,
        val unit: String?,
        val price: Int = 0,
        val qty: Double = 0.0,
    )
}
