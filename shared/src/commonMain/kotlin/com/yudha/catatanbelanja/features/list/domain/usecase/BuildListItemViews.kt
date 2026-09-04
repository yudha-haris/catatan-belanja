package com.yudha.catatanbelanja.features.list.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.list.domain.model.ShoppingListItemView

/**
 * Ticked lines sink to the bottom so what is still missing stays under the thumb. The sort is
 * stable, so unticking a line puts it back exactly where it was written.
 */
class BuildListItemViews(private val findItemCategory: FindItemCategory) {
    operator fun invoke(items: List<ShoppingListItem>): List<ShoppingListItemView> = items
        .sortedBy { it.isChecked }
        .map { item ->
            ShoppingListItemView(item = item, emoji = findItemCategory.emojiFor(item.name))
        }
}
