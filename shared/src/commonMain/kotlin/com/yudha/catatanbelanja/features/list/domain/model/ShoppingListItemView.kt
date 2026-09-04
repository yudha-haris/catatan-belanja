package com.yudha.catatanbelanja.features.list.domain.model

import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem

/** One line of the plan, with the category emoji resolved out of the composable's way. */
data class ShoppingListItemView(
    val item: ShoppingListItem,
    val emoji: String,
)
