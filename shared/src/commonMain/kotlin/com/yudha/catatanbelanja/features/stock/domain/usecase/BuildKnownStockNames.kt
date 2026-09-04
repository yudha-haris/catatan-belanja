package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

/**
 * The prototype's `knownNames()` as the stock editor's autocomplete sees it: the catalog first,
 * then anything ever actually bought, deduped by normalized name.
 */
class BuildKnownStockNames {
    operator fun invoke(sessions: List<ShoppingSession>): List<String> {
        val byKey = linkedMapOf<String, String>()
        CatalogData.categories
            .flatMap { it.items }
            .forEach { name -> byKey[name.normalized()] = name }
        sessions
            .flatMap { it.items }
            .forEach { item -> byKey.getOrPut(item.name.normalized()) { item.name } }
        return byKey.values.toList()
    }
}
