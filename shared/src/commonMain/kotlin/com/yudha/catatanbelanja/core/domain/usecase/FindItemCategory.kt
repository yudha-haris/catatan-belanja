package com.yudha.catatanbelanja.core.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository

/**
 * Which category an item name belongs to, and therefore which emoji stands in front of it.
 *
 * Reads [CatalogRepository.current] rather than suspending: this runs inside pure mappers, once
 * per receipt row, and the catalog it consults is already in memory.
 */
class FindItemCategory(private val catalogRepository: CatalogRepository) {
    operator fun invoke(name: String): CatalogCategory? {
        val key = name.normalized()
        if (key.isEmpty()) return null
        return catalogRepository.current.firstOrNull { category ->
            category.items.any { it.name.normalized() == key }
        }
    }

    fun emojiFor(name: String): String = invoke(name)?.emoji ?: CatalogData.FALLBACK_EMOJI

    /** The emoji filed against a category *name*, for the rows that group by category. */
    fun emojiOfCategory(name: String): String =
        catalogRepository.current.firstOrNull { it.name == name }?.emoji ?: CatalogData.FALLBACK_EMOJI
}
