package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.common.monthKeyOf
import com.yudha.catatanbelanja.core.common.toLocalDate
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.math.roundToInt

/**
 * The arithmetic every dashboard derivation shares. Kept in one file so the summary tab and the
 * three report pages cannot drift into two different ideas of what "rata-rata" means.
 */

/** Finished sessions always carry an end; the fallback keeps the sort total for malformed rows. */
internal fun ShoppingSession.endedMillis(): Long = endedAt ?: startedAt

internal fun ShoppingSession.total(): Int = items.sumOf { it.price }

/** The window [range] covers, newest month first — empty for [ReportRange.ALL], which spans all. */
internal fun monthKeysIn(range: ReportRange, nowMillis: Long): List<String> {
    val months = when (range) {
        ReportRange.MONTH -> 1
        ReportRange.THREE_MONTHS -> 3
        ReportRange.SIX_MONTHS -> 6
        ReportRange.ALL -> return emptyList()
    }
    val today = nowMillis.toLocalDate()
    return (0 until months).map { back ->
        val at = today.minus(back, DateTimeUnit.MONTH)
        monthKeyOf(at.year, at.monthNumber)
    }
}

/** The calendar month before the one [nowMillis] falls in. */
internal fun previousMonthKey(nowMillis: Long): String {
    val previous = nowMillis.toLocalDate().minus(1, DateTimeUnit.MONTH)
    return monthKeyOf(previous.year, previous.monthNumber)
}

/** The sessions of [range]. Calendar-aligned: "3 bulan" is three month columns, not 90 days. */
internal fun List<ShoppingSession>.inRange(
    range: ReportRange,
    nowMillis: Long,
): List<ShoppingSession> {
    val keys = monthKeysIn(range, nowMillis)
    if (keys.isEmpty()) return this
    return filter { keys.contains(it.endedMillis().toMonthKey()) }
}

internal fun ratioOf(value: Int, max: Int): Float {
    if (max <= 0) return 0f
    return value.toFloat() / max.toFloat()
}

internal fun percentOf(part: Int, whole: Int): Int {
    if (whole <= 0) return 0
    return (part.toDouble() / whole.toDouble() * 100.0).roundToInt()
}

internal fun percentChange(current: Int, previous: Int): Int {
    if (previous == 0) return 0
    return ((current - previous).toDouble() / previous.toDouble() * 100.0).roundToInt()
}

internal fun averageOf(total: Int, count: Int): Int {
    if (count == 0) return 0
    return total / count
}
