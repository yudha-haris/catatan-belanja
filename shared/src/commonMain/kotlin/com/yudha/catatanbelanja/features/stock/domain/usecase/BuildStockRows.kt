package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.stock.domain.model.StockRowView
import com.yudha.catatanbelanja.features.stock.domain.model.StockShadow

/**
 * Turns raw stock items into name-sorted rows with the low flag, the bar ratio, the labels, and
 * the estimate drawn alongside them.
 *
 * The sections a row lands in and the warning it carries are decided by the *stored* quantity
 * throughout. An estimate may whisper that something is probably running low; it may not move the
 * row into "Perlu dibeli", because sending the user to the shop on a guess is how a guess stops
 * being welcome.
 */
class BuildStockRows(
    private val findItemCategory: FindItemCategory,
    private val estimateStockRate: EstimateStockRate,
    private val estimateCurrentStock: EstimateCurrentStock,
    private val clock: Clock,
) {

    operator fun invoke(
        items: List<StockItem>,
        rates: Map<String, StockRate> = emptyMap(),
        readings: Map<String, List<StockReading>> = emptyMap(),
    ): List<StockRowView> {
        val now = clock.nowMillis()
        return items
            .sortedBy { it.name.normalized() }
            .map { item -> toRow(item, rates[item.id], readings[item.id].orEmpty(), now) }
    }

    private fun toRow(
        item: StockItem,
        rate: StockRate?,
        readings: List<StockReading>,
        now: Long,
    ): StockRowView {
        val isLow = isLow(item)
        val isOut = item.qty <= 0.0
        val scale = scaleOf(item)
        val settled = rate ?: StockRate(itemId = item.id)
        val auto = estimateStockRate(item = item, readings = readings, nowMillis = now)
        val shadow = estimateCurrentStock(
            item = item,
            rate = settled,
            auto = auto,
            scale = scale,
            nowMillis = now,
        )
        return StockRowView(
            item = item,
            emoji = findItemCategory.emojiFor(item.name),
            ratio = (item.qty / scale).coerceIn(0.0, 1.0).toFloat(),
            isLow = isLow,
            isOut = isOut,
            subtitle = subtitleOf(item, shadow),
            alert = alertOf(isLow = isLow, isOut = isOut, shadow = shadow),
            shadow = shadow,
            rate = settled,
            autoEstimate = auto,
        )
    }

    /** With a reminder set, "low" means at or under it; without one, only an empty shelf counts. */
    private fun isLow(item: StockItem): Boolean {
        val minQty = item.minQty ?: return item.qty <= 0.0
        return item.qty <= minQty
    }

    /**
     * The bar fills against the high-water mark, but never against less than the current qty,
     * the reminder threshold, or 1 — otherwise a single-unit item would render as a full bar
     * against a zero denominator. The estimate's ghost mark shares this scale.
     */
    private fun scaleOf(item: StockItem): Double =
        maxOf(item.fullQty, item.qty, item.minQty ?: 0.0, 1.0)

    /** An estimate is the most useful thing a row can say, so it takes the subtitle when present. */
    private fun subtitleOf(item: StockItem, shadow: StockShadow?): StockRowView.Subtitle {
        if (shadow != null) return StockRowView.Subtitle.ESTIMATE
        if (item.minQty != null) return StockRowView.Subtitle.REMINDER
        return StockRowView.Subtitle.UPDATED
    }

    private fun alertOf(isLow: Boolean, isOut: Boolean, shadow: StockShadow?): StockRowView.Alert {
        if (isOut) return StockRowView.Alert.NEED_BUY
        if (isLow) return StockRowView.Alert.RUNNING_LOW
        if (shadow?.isBelowMin == true) return StockRowView.Alert.MAYBE_LOW
        return StockRowView.Alert.NONE
    }
}
