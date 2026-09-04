package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.stock.domain.model.StockUsageRow
import kotlin.math.round

private const val DEFAULT_UNIT = "pcs"
private const val CENTS = 100.0

/**
 * Reads a month's stock check as a usage report: what came in that month, what was still
 * there at the end, and therefore roughly what got consumed since the previous check.
 */
class CalculateStockUsage(private val findItemCategory: FindItemCategory) {

    operator fun invoke(
        log: StockCheckLog,
        allLogs: List<StockCheckLog>,
        sessions: List<ShoppingSession>,
    ): List<StockUsageRow> {
        val previous = previousLogOf(log, allLogs)
        val bought = boughtInMonth(log.month, sessions)
        return log.entries.map { entry ->
            val boughtQty = bought[entry.key()] ?: 0.0
            toRow(
                entry = entry,
                boughtQty = boughtQty,
                previousQty = previousQtyOf(previous, entry),
            )
        }
    }

    /** The most recent check older than [log] — the baseline every usage figure counts down from. */
    fun previousLogOf(log: StockCheckLog, allLogs: List<StockCheckLog>): StockCheckLog? =
        allLogs.filter { it.checkedAt < log.checkedAt }.maxByOrNull { it.checkedAt }

    private fun toRow(
        entry: StockCheckEntry,
        boughtQty: Double,
        previousQty: Double?,
    ): StockUsageRow = StockUsageRow(
        name = entry.name,
        emoji = findItemCategory.emojiFor(entry.name),
        unit = entry.unit,
        boughtQty = boughtQty,
        hasBought = boughtQty > 0.0,
        remainingQty = entry.qty,
        isOut = entry.qty <= 0.0,
        usedQty = usedQty(previousQty = previousQty, boughtQty = boughtQty, remaining = entry.qty),
    )

    /**
     * used ≈ what was there + what was bought − what is left. Without an earlier check and
     * without a purchase there is nothing to subtract from, so the figure is simply not shown.
     */
    private fun usedQty(previousQty: Double?, boughtQty: Double, remaining: Double): Double? {
        if (previousQty == null && boughtQty <= 0.0) return null
        val used = (previousQty ?: 0.0) + boughtQty - remaining
        return round(maxOf(0.0, used) * CENTS) / CENTS
    }

    private fun previousQtyOf(previous: StockCheckLog?, entry: StockCheckEntry): Double? =
        previous?.entries?.firstOrNull { it.key() == entry.key() }?.qty

    /** Purchases only count towards an entry when the unit matches — 2 kg is not 2 bungkus. */
    private fun boughtInMonth(
        month: String,
        sessions: List<ShoppingSession>,
    ): Map<Pair<String, String>, Double> {
        val totals = mutableMapOf<Pair<String, String>, Double>()
        sessions
            .filter { it.endedAt?.toMonthKey() == month }
            .flatMap { it.items }
            .forEach { item ->
                val qty = item.qty ?: return@forEach
                if (qty <= 0.0) return@forEach
                val key = item.name.normalized() to (item.unit ?: DEFAULT_UNIT)
                totals[key] = (totals[key] ?: 0.0) + qty
            }
        return totals
    }

    private fun StockCheckEntry.key(): Pair<String, String> = name.normalized() to unit
}
