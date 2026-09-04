package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

private const val BRAND_LIMIT = 6

/** The prototype's `brandsFor(name)`: notes previously used for this item, newest first. */
class FindBrandSuggestions {
    operator fun invoke(name: String, sessions: List<ShoppingSession>): List<String> {
        val key = name.normalized()
        if (key.isEmpty()) return emptyList()

        return sessions.asSequence()
            .sortedByDescending { it.endedAt ?: it.startedAt }
            .flatMap { it.items.asSequence() }
            .filter { it.name.normalized() == key && it.note.isNotBlank() }
            .map { it.note }
            .distinctBy { it.normalized() }
            .take(BRAND_LIMIT)
            .toList()
    }
}
