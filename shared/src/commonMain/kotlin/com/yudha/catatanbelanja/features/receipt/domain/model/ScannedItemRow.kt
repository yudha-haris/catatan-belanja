package com.yudha.catatanbelanja.features.receipt.domain.model

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem

/**
 * One line of the scan review list: what the model read, plus the emoji the catalog files that
 * name under, so the draft looks like the receipt it is about to become.
 */
data class ScannedItemRow(
    val item: ShoppingItem,
    val emoji: String,
)
