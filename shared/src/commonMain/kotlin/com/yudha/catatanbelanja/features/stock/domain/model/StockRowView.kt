package com.yudha.catatanbelanja.features.stock.domain.model

import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate

/** One row of the Stok list: the item plus every label decision already made for the screen. */
data class StockRowView(
    val item: StockItem,
    val emoji: String,
    val ratio: Float,
    val isLow: Boolean,
    val isOut: Boolean,
    val subtitle: Subtitle,
    val alert: Alert,
    /**
     * What the app reckons is left right now, or null when it has nothing to add. Never folded
     * into [item] or into [ratio]: the row goes on showing the quantity somebody actually wrote
     * down, and the estimate is drawn beside it.
     */
    val shadow: StockShadow? = null,
    /** The item's own setting, carried so the rate sheet opens already filled in. */
    val rate: StockRate = StockRate(itemId = item.id),
    /** What the history alone suggests — shown even under a manual rate, as a second opinion. */
    val autoEstimate: StockRateEstimate? = null,
) {
    /** Which subtitle copy the row carries. */
    enum class Subtitle { REMINDER, UPDATED, ESTIMATE }

    /** The small trailing warning under the quantity. */
    enum class Alert {
        NONE,
        RUNNING_LOW,
        NEED_BUY,

        /** The stored quantity is still fine, but the estimate has crossed the reminder line. */
        MAYBE_LOW,
    }
}
