package com.yudha.catatanbelanja.core.domain.model

/**
 * One line of a [ShoppingList]. Deliberately thinner than [ShoppingItem]: planning is a list of
 * names, not of prices. [note] is the optional "5 kg" / "yang merek A" the user may add later.
 */
data class ShoppingListItem(
    val id: String,
    val name: String,
    val note: String = "",
    val isChecked: Boolean = false,
)
