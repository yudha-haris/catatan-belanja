package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem

/** The prototype's `addItem()` item shape: the unit only travels with a real quantity. */
class CreateShoppingItem(private val idGenerator: IdGenerator) {
    operator fun invoke(
        name: String,
        qty: Double?,
        unit: String,
        note: String,
        price: Int,
    ): ShoppingItem {
        val quantity = qty?.takeIf { it > 0.0 }
        return ShoppingItem(
            id = idGenerator.next(),
            name = name.trim().capitalizeWords(),
            price = price,
            qty = quantity,
            unit = unit.takeIf { quantity != null },
            note = note.trim(),
        )
    }
}
