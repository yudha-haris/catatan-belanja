package com.yudha.catatanbelanja.features.receipt.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.receipt.domain.model.ScannedItemRow

/** Draft items to review rows. Rebuilt on every edit, because a rename changes the emoji. */
class BuildScannedRows(private val findItemCategory: FindItemCategory) {
    operator fun invoke(items: List<ShoppingItem>): List<ScannedItemRow> = items.map { item ->
        ScannedItemRow(item = item, emoji = findItemCategory.emojiFor(item.name))
    }
}
