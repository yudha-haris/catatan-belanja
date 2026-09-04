package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingBar
import com.yudha.catatanbelanja.features.dashboard.domain.model.TopItem
import kotlin.math.abs

private const val RECENT_BAR_COUNT = 8
private const val TOP_ITEM_COUNT = 5

/**
 * The prototype's `dashView()` as one pure derivation over the finished sessions. Pure on purpose:
 * the ViewModel loads once and re-runs this whenever the scope changes, so the toggle never hits
 * the database.
 *
 * The price trend used to live here too. It moved to `BuildPriceTrend`, which is the only thing
 * that knows about an item's saved measurement basis and its manual quantity corrections — leaving
 * a second, simpler copy here is how the card and the trend page would end up disagreeing.
 */
class BuildDashboardData(
    private val clock: Clock,
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(
        sessions: List<ShoppingSession>,
        scope: DashboardScope,
    ): DashboardData {
        val newestFirst = sessions.sortedByDescending { it.endedMillis() }
        if (newestFirst.isEmpty()) return DashboardData()

        val now = clock.nowMillis()
        val monthKey = now.toMonthKey()
        val previousKey = previousMonthKey(now)
        val thisMonth = newestFirst.filter { it.endedMillis().toMonthKey() == monthKey }
        val monthTotal = thisMonth.sumOf { it.total() }
        val previousMonthTotal = newestFirst
            .filter { it.endedMillis().toMonthKey() == previousKey }
            .sumOf { it.total() }

        val scoped = scopedSessions(newestFirst, thisMonth, scope)

        return DashboardData(
            monthKey = monthKey,
            monthTotal = monthTotal,
            previousMonthTotal = previousMonthTotal,
            monthDeltaPercent = abs(percentChange(monthTotal, previousMonthTotal)),
            hasMonthComparison = previousMonthTotal > 0,
            isMonthSpendingUp = previousMonthTotal > 0 && monthTotal > previousMonthTotal,
            isMonthSpendingDown = monthTotal < previousMonthTotal,
            monthSessionCount = thisMonth.size,
            monthAverage = averageOf(monthTotal, thisMonth.size),
            hasMonthAverage = thisMonth.isNotEmpty(),
            recentBars = recentBars(newestFirst),
            topItems = topItems(scoped),
        )
    }

    /** "Bulan ini" falls back to every session while the current month is still empty. */
    private fun scopedSessions(
        all: List<ShoppingSession>,
        thisMonth: List<ShoppingSession>,
        scope: DashboardScope,
    ): List<ShoppingSession> = when (scope) {
        DashboardScope.ALL -> all
        DashboardScope.MONTH -> thisMonth.ifEmpty { all }
    }

    private fun recentBars(newestFirst: List<ShoppingSession>): List<SpendingBar> {
        val oldestFirst = newestFirst.take(RECENT_BAR_COUNT).asReversed()
        val tallest = oldestFirst.maxOfOrNull { it.total() } ?: 0
        return oldestFirst.map { session ->
            val total = session.total()
            SpendingBar(
                sessionId = session.id,
                endedAt = session.endedMillis(),
                total = total,
                ratio = ratioOf(total, tallest),
                isHighest = total == tallest,
            )
        }
    }

    private fun topItems(scoped: List<ShoppingSession>): List<TopItem> {
        val scopedTotal = scoped.sumOf { it.total() }
        val ranked = scoped
            .flatMap { it.items }
            .groupBy { it.name.normalized() }
            .values
            .sortedByDescending { group -> group.sumOf { it.price } }
            .take(TOP_ITEM_COUNT)
        val leaderTotal = ranked.firstOrNull()?.sumOf { it.price } ?: 0
        return ranked.map { group ->
            val total = group.sumOf { it.price }
            val name = group.first().name
            TopItem(
                name = name,
                emoji = findItemCategory.emojiFor(name),
                total = total,
                purchaseCount = group.size,
                ratio = ratioOf(total, leaderTotal),
                sharePercent = percentOf(total, scopedTotal),
            )
        }
    }
}
