package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendCandidate

private const val MIN_TREND_PURCHASES = 2

/** The price-trend picker's list: everything bought twice or more, most bought first. */
class BuildTrendCandidates(
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(sessions: List<ShoppingSession>): List<TrendCandidate> {
        val newestFirst = sessions.sortedByDescending { it.endedMillis() }
        return newestFirst
            .flatMap { session -> session.items.map { session to it } }
            .groupBy { (_, item) -> item.name.normalized() }
            .values
            .filter { it.size >= MIN_TREND_PURCHASES }
            .sortedByDescending { it.size }
            .map { group ->
                val (newestSession, newestItem) = group.first()
                TrendCandidate(
                    name = newestItem.name,
                    emoji = findItemCategory.emojiFor(newestItem.name),
                    purchaseCount = group.size,
                    lastPrice = newestItem.price,
                    lastBoughtAt = newestSession.endedMillis(),
                )
            }
    }
}
