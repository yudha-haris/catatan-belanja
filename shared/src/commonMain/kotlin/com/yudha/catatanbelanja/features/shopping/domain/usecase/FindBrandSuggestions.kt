package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.BrandPreset
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

private const val HISTORY_LIMIT = 6
private const val TOTAL_LIMIT = 12

/**
 * The prototype's `brandsFor(name)`: notes previously used for this item, newest first — now with
 * the "merk" preset list behind them.
 *
 * History comes first and keeps its own cap: a brand this very item was bought under is a better
 * guess than one the user merely wrote down once. The presets fill whatever room is left, so a
 * fresh install with no history still has something to offer.
 */
class FindBrandSuggestions {
    operator fun invoke(
        name: String,
        sessions: List<ShoppingSession>,
        presets: List<BrandPreset>,
    ): List<String> {
        val key = name.normalized()
        if (key.isEmpty()) return emptyList()

        val fromHistory = sessions.asSequence()
            .sortedByDescending { it.endedAt ?: it.startedAt }
            .flatMap { it.items.asSequence() }
            .filter { it.name.normalized() == key && it.note.isNotBlank() }
            .map { it.note }
            .distinctBy { it.normalized() }
            .take(HISTORY_LIMIT)
            .toList()

        return (fromHistory + presets.map { it.name })
            .distinctBy { it.normalized() }
            .take(TOTAL_LIMIT)
    }
}
