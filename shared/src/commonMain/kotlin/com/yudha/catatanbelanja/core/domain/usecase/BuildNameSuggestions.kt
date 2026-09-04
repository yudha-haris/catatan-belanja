package com.yudha.catatanbelanja.core.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

private const val SEARCH_LIMIT = 8
private const val FREQUENT_LIMIT = 8
private const val FREQUENT_TOTAL_LIMIT = 14

/** The prototype's `knownNames()`, `frequent()` and the `renderSugg()` filter. */
class BuildNameSuggestions {
    /** Catalog items ∪ every name ever purchased, deduped by normalized name. */
    fun knownNames(sessions: List<ShoppingSession>): List<String> {
        val byKey = LinkedHashMap<String, String>()
        CatalogData.categories.forEach { category ->
            category.items.forEach { byKey[it.normalized()] = it }
        }
        sessions.forEach { session ->
            session.items.forEach { item ->
                val key = item.name.normalized()
                if (!byKey.containsKey(key)) byKey[key] = item.name
            }
        }
        return byKey.values.toList()
    }

    /**
     * Top [FREQUENT_LIMIT] names by purchase count, with the low-stock names and the "belanja
     * lagi" list pushed in front of them.
     */
    fun frequent(
        sessions: List<ShoppingSession>,
        lowStockNames: List<String>,
        repeatNames: List<String>,
    ): List<String> {
        val counts = LinkedHashMap<String, Int>()
        val labels = LinkedHashMap<String, String>()
        sessions.forEach { session ->
            session.items.forEach { item ->
                val key = item.name.normalized()
                counts[key] = (counts[key] ?: 0) + 1
                if (!labels.containsKey(key)) labels[key] = item.name
            }
        }
        val base = counts.entries
            .sortedByDescending { it.value }
            .take(FREQUENT_LIMIT)
            .mapNotNull { labels[it.key] }
        if (lowStockNames.isEmpty() && repeatNames.isEmpty()) return base

        return (lowStockNames + repeatNames + base)
            .distinctBy { it.normalized() }
            .take(FREQUENT_TOTAL_LIMIT)
    }

    /** Names containing [query], prefix matches first, capped at [SEARCH_LIMIT]. */
    operator fun invoke(query: String, knownNames: List<String>): List<String> {
        val key = query.normalized()
        if (key.isEmpty()) return emptyList()
        return knownNames
            .filter { it.normalized().contains(key) }
            .sortedBy { if (it.normalized().startsWith(key)) 0 else 1 }
            .take(SEARCH_LIMIT)
    }

    /** False means the screen offers the "＋ barang baru" chip. */
    fun hasExactMatch(query: String, knownNames: List<String>): Boolean {
        val key = query.normalized()
        if (key.isEmpty()) return true
        return knownNames.any { it.normalized() == key }
    }
}
