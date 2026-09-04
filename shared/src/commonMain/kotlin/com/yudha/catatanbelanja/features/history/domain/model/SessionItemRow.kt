package com.yudha.catatanbelanja.features.history.domain.model

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem

/**
 * One row of the session detail list. [previousPrice] comes from the previous finished session,
 * matched by normalized name. [priceDeltaAmount] is the absolute gap against it — null when there
 * is nothing to compare against or the price did not move (no arrow is drawn) — and [isPriceUp]
 * says which arrow that gap earns.
 */
data class SessionItemRow(
    val item: ShoppingItem,
    val emoji: String,
    val unitPrice: Int?,
    val previousPrice: Int?,
    val priceDeltaAmount: Int?,
    val isPriceUp: Boolean,
)
