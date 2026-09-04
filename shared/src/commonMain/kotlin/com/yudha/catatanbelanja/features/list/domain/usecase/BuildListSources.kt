package com.yudha.catatanbelanja.features.list.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.features.list.domain.model.ListSource

/**
 * The "buat daftar" menu: a blank list, last trip's items, whatever is running out, and every
 * saved template. A source with nothing to add is left out rather than shown empty.
 */
class BuildListSources {
    operator fun invoke(
        lastSession: ShoppingSession?,
        stock: List<StockItem>,
        templates: List<ShoppingList>,
    ): List<ListSource> {
        val sources = mutableListOf(ListSource(kind = ListSource.Kind.BLANK, names = emptyList()))

        val lastNames = lastSession?.items.orEmpty().map { it.name }
        if (lastNames.isNotEmpty()) {
            sources += ListSource(
                kind = ListSource.Kind.LAST_SESSION,
                names = lastNames,
                label = lastSession?.name.orEmpty().ifBlank { lastSession?.store.orEmpty() },
            )
        }

        val lowNames = stock.filter { it.isLow() }.map { it.name }
        if (lowNames.isNotEmpty()) {
            sources += ListSource(kind = ListSource.Kind.LOW_STOCK, names = lowNames)
        }

        templates.forEach { template ->
            if (template.items.isEmpty()) return@forEach
            sources += ListSource(
                kind = ListSource.Kind.TEMPLATE,
                names = template.items.map { it.name },
                label = template.name,
                templateId = template.id,
            )
        }
        return sources
    }

    /** The Stok tab's own rule, repeated here rather than reached for across features. */
    private fun StockItem.isLow(): Boolean = minQty?.let { qty <= it } ?: (qty <= 0.0)
}
