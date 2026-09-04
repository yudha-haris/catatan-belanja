package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.features.dashboard.domain.model.MonthSpending
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingReportData
import com.yudha.catatanbelanja.features.dashboard.domain.model.TripSpending
import kotlin.math.abs

private const val CHART_BAR_COUNT = 6

/**
 * The "Laporan belanja" page: the eight bars on the summary tab, opened out into every trip in the
 * window plus the month-by-month shape behind them.
 *
 * Pure, like [BuildDashboardData] — the range chips re-derive rather than re-query.
 */
class BuildSpendingReport(
    private val clock: Clock,
) {
    operator fun invoke(
        sessions: List<ShoppingSession>,
        range: ReportRange,
    ): SpendingReportData {
        val newestFirst = sessions
            .sortedByDescending { it.endedMillis() }
            .inRange(range, clock.nowMillis())
        if (newestFirst.isEmpty()) return SpendingReportData(range = range)

        val total = newestFirst.sumOf { it.total() }
        val months = months(newestFirst)
        val highest = newestFirst.maxBy { it.total() }

        return SpendingReportData(
            range = range,
            total = total,
            tripCount = newestFirst.size,
            tripAverage = averageOf(total, newestFirst.size),
            monthCount = months.size,
            monthAverage = averageOf(total, months.size),
            highestTotal = highest.total(),
            highestSessionId = highest.id,
            hasHighest = true,
            months = months.asReversed(),
            monthBars = bars(months),
            trips = trips(newestFirst),
            hasAnyTrip = true,
        )
    }

    /** Oldest first, every month in the window. The chart takes its own slice off the end. */
    private fun months(newestFirst: List<ShoppingSession>): List<MonthSpending> {
        val grouped = newestFirst
            .groupBy { it.endedMillis().toMonthKey() }
            .toList()
            .sortedBy { it.first }
        val tallest = grouped.maxOfOrNull { group -> group.second.sumOf { it.total() } } ?: 0

        return grouped.mapIndexed { index, (monthKey, sessionsOfMonth) ->
            val total = sessionsOfMonth.sumOf { it.total() }
            val previous = grouped.getOrNull(index - 1)?.second?.sumOf { it.total() } ?: 0
            MonthSpending(
                monthKey = monthKey,
                total = total,
                sessionCount = sessionsOfMonth.size,
                average = averageOf(total, sessionsOfMonth.size),
                ratio = ratioOf(total, tallest),
                isHighest = total == tallest,
                deltaPercent = abs(percentChange(total, previous)),
                hasDelta = previous > 0,
                isUp = previous > 0 && total > previous,
                isDown = previous > 0 && total < previous,
            )
        }
    }

    /**
     * The last few months, re-scaled against each other. Keeping the full window's ratio would
     * flatten every bar the moment one older month dwarfed the rest, which is the one thing a
     * six-bar chart cannot afford.
     */
    private fun bars(oldestFirst: List<MonthSpending>): List<MonthSpending> {
        val window = oldestFirst.takeLast(CHART_BAR_COUNT)
        val tallest = window.maxOfOrNull { it.total } ?: 0
        return window.map { month ->
            month.copy(ratio = ratioOf(month.total, tallest), isHighest = month.total == tallest)
        }
    }

    /** Newest first. The delta compares against the trip before it, which is the one below it. */
    private fun trips(newestFirst: List<ShoppingSession>): List<TripSpending> =
        newestFirst.mapIndexed { index, session ->
            val total = session.total()
            val previous = newestFirst.getOrNull(index + 1)?.total() ?: 0
            val hasPrevious = index < newestFirst.lastIndex
            TripSpending(
                sessionId = session.id,
                name = session.name,
                store = session.store,
                hasName = session.name.isNotBlank(),
                hasStore = session.store.isNotBlank(),
                endedAt = session.endedMillis(),
                total = total,
                itemCount = session.items.size,
                deltaAmount = total - previous,
                hasDelta = hasPrevious,
                isUp = hasPrevious && total > previous,
                isDown = hasPrevious && total < previous,
            )
        }
}
