package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.stock.domain.model.StockRowView

/** Turns raw stock items into name-sorted rows with the low flag, the bar ratio and the labels. */
class BuildStockRows(private val findItemCategory: FindItemCategory) {

    operator fun invoke(items: List<StockItem>): List<StockRowView> =
        items.sortedBy { it.name.normalized() }.map(::toRow)

    private fun toRow(item: StockItem): StockRowView {
        val isLow = isLow(item)
        val isOut = item.qty <= 0.0
        return StockRowView(
            item = item,
            emoji = findItemCategory.emojiFor(item.name),
            ratio = ratioOf(item),
            isLow = isLow,
            isOut = isOut,
            subtitle = subtitleOf(item),
            alert = alertOf(isLow = isLow, isOut = isOut),
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
     * against a zero denominator.
     */
    private fun ratioOf(item: StockItem): Float {
        val full = maxOf(item.fullQty, item.qty, item.minQty ?: 0.0, 1.0)
        return (item.qty / full).coerceIn(0.0, 1.0).toFloat()
    }

    private fun subtitleOf(item: StockItem): StockRowView.Subtitle {
        if (item.minQty != null) return StockRowView.Subtitle.REMINDER
        return StockRowView.Subtitle.UPDATED
    }

    private fun alertOf(isLow: Boolean, isOut: Boolean): StockRowView.Alert {
        if (!isLow) return StockRowView.Alert.NONE
        if (isOut) return StockRowView.Alert.NEED_BUY
        return StockRowView.Alert.RUNNING_LOW
    }
}
