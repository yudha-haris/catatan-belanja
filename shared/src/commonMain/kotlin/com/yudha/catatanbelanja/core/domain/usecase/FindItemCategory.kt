package com.yudha.catatanbelanja.core.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ItemCategory

class FindItemCategory {
    operator fun invoke(name: String): ItemCategory? {
        val key = name.normalized()
        if (key.isEmpty()) return null
        return CatalogData.categories.firstOrNull { category ->
            category.items.any { it.normalized() == key }
        }
    }

    fun emojiFor(name: String): String = invoke(name)?.emoji ?: CatalogData.FALLBACK_EMOJI
}
