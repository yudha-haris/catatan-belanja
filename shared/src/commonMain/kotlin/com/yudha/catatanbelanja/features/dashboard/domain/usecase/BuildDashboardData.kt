package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.monthKeyOf
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.toLocalDate
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingBar
import com.yudha.catatanbelanja.features.dashboard.domain.model.TopItem
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPoint
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.math.abs
import kotlin.math.roundToInt

private const val RECENT_BAR_COUNT = 8
private const val TOP_ITEM_COUNT = 5
private const val MIN_TREND_PURCHASES = 2

/**
 * The prototype's `dashView()` + `trendSvg()` as one pure derivation over the finished sessions.
 * Pure on purpose: the ViewModel loads once and re-runs this whenever the scope or the trend item
 * changes, so neither toggle hits the database.
 */
class BuildDashboardData(
    private val clock: Clock,
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(
        sessions: List<ShoppingSession>,
        scope: DashboardScope,
        trendName: String?,
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
        val trendableNames = trendableNames(newestFirst)
        val selectedTrendName = resolveTrendName(trendableNames, trendName)
        val trendPrices = trendPrices(newestFirst, selectedTrendName)

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
            trendableNames = trendableNames,
            trendName = selectedTrendName,
            trendPoints = trendPoints(trendPrices),
            hasTrend = trendPrices.size >= MIN_TREND_PURCHASES,
            trendFirstPrice = trendPrices.firstOrNull()?.second ?: 0,
            trendLastPrice = trendPrices.lastOrNull()?.second ?: 0,
            trendDeltaPercent = percentChange(
                current = trendPrices.lastOrNull()?.second ?: 0,
                previous = trendPrices.firstOrNull()?.second ?: 0,
            ),
            isTrendUp = isTrendUp(trendPrices),
            isTrendDown = isTrendDown(trendPrices),
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

    private fun trendableNames(newestFirst: List<ShoppingSession>): List<String> = newestFirst
        .flatMap { it.items }
        .groupBy { it.name.normalized() }
        .values
        .filter { it.size >= MIN_TREND_PURCHASES }
        .sortedByDescending { it.size }
        .map { it.first().name }

    private fun resolveTrendName(trendableNames: List<String>, requested: String?): String? {
        val key = requested?.normalized().orEmpty()
        val match = trendableNames.firstOrNull { it.normalized() == key }
        if (match != null) return match
        return trendableNames.firstOrNull()
    }

    /** Every price paid for [name], oldest purchase first. */
    private fun trendPrices(
        newestFirst: List<ShoppingSession>,
        name: String?,
    ): List<Pair<Long, Int>> {
        if (name == null) return emptyList()
        val key = name.normalized()
        return newestFirst.asReversed().mapNotNull { session ->
            val item = session.items.firstOrNull { it.name.normalized() == key }
            item?.let { session.endedMillis() to it.price }
        }
    }

    private fun trendPoints(prices: List<Pair<Long, Int>>): List<TrendPoint> {
        if (prices.size < MIN_TREND_PURCHASES) return emptyList()
        val cheapest = prices.minOf { it.second }
        val priciest = prices.maxOf { it.second }
        // A flat series would divide by zero; pin it to the baseline like the prototype.
        val range = (priciest - cheapest).takeIf { it > 0 } ?: 1
        return prices.map { (endedAt, price) ->
            TrendPoint(
                endedAt = endedAt,
                price = price,
                ratio = (price - cheapest).toFloat() / range.toFloat(),
            )
        }
    }

    private fun isTrendUp(prices: List<Pair<Long, Int>>): Boolean {
        if (prices.size < MIN_TREND_PURCHASES) return false
        return prices.last().second > prices.first().second
    }

    private fun isTrendDown(prices: List<Pair<Long, Int>>): Boolean {
        if (prices.size < MIN_TREND_PURCHASES) return false
        return prices.last().second < prices.first().second
    }
}

private fun ShoppingSession.total(): Int = items.sumOf { it.price }

/** Finished sessions always carry an end; the fallback keeps the sort total for malformed rows. */
private fun ShoppingSession.endedMillis(): Long = endedAt ?: startedAt

private fun previousMonthKey(nowMillis: Long): String {
    val previous = nowMillis.toLocalDate().minus(1, DateTimeUnit.MONTH)
    return monthKeyOf(previous.year, previous.monthNumber)
}

private fun ratioOf(value: Int, max: Int): Float {
    if (max <= 0) return 0f
    return value.toFloat() / max.toFloat()
}

private fun percentOf(part: Int, whole: Int): Int {
    if (whole <= 0) return 0
    return (part.toDouble() / whole.toDouble() * 100.0).roundToInt()
}

private fun percentChange(current: Int, previous: Int): Int {
    if (previous == 0) return 0
    return ((current - previous).toDouble() / previous.toDouble() * 100.0).roundToInt()
}

private fun averageOf(total: Int, count: Int): Int {
    if (count == 0) return 0
    return total / count
}
