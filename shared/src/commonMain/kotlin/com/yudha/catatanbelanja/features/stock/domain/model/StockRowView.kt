package com.yudha.catatanbelanja.features.stock.domain.model

import com.yudha.catatanbelanja.core.domain.model.StockItem

/** One row of the Stok list: the item plus every label decision already made for the screen. */
data class StockRowView(
    val item: StockItem,
    val emoji: String,
    val ratio: Float,
    val isLow: Boolean,
    val isOut: Boolean,
    val subtitle: Subtitle,
    val alert: Alert,
) {
    /** Which of the two subtitle copies the row carries. */
    enum class Subtitle { REMINDER, UPDATED }

    /** The small trailing warning under the quantity. */
    enum class Alert { NONE, RUNNING_LOW, NEED_BUY }
}
