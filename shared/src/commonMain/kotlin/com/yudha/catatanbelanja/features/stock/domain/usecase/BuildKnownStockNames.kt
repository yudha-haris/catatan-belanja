package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository

/**
 * The prototype's `knownNames()` as the stock editor's autocomplete sees it: the catalog first,
 * then anything ever actually bought, deduped by normalized name.
 */
class BuildKnownStockNames(private val catalogRepository: CatalogRepository) {
    operator fun invoke(sessions: List<ShoppingSession>): List<String> {
        val byKey = linkedMapOf<String, String>()
        catalogRepository.current
            .flatMap { it.items }
            .forEach { item -> byKey[item.name.normalized()] = item.name }
        sessions
            .flatMap { it.items }
            .forEach { item -> byKey.getOrPut(item.name.normalized()) { item.name } }
        return byKey.values.toList()
    }
}
