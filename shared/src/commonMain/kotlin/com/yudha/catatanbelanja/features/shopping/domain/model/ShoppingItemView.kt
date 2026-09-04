package com.yudha.catatanbelanja.features.shopping.domain.model

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem

/**
 * A cart row, pre-decided. Every nullable field means "do not render this part" — the
 * composable formats what is here and asks no questions.
 */
data class ShoppingItemView(
    val item: ShoppingItem,
    val emoji: String,
    /** Non-null together with [unit] when the "2 liter" part shows. */
    val qty: Double?,
    val unit: String?,
    /** Price per unit, only when the quantity is not exactly one. */
    val unitPrice: Int?,
    /** Empty when there is no "🏷 merk" part. */
    val note: String,
    /** Price change against the same item in the previous session; null when there is none. */
    val deltaFromPrevious: Int?,
)
