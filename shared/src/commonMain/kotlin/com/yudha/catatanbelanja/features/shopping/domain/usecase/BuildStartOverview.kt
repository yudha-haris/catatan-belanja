package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.shopping.domain.model.Greeting
import com.yudha.catatanbelanja.features.shopping.domain.model.StartOverview
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val RECENT_LIMIT = 3
private const val STORE_LIMIT = 5

/** The prototype's `startView()` header maths, in one pass over the finished sessions. */
class BuildStartOverview(
    private val clock: Clock,
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(
        finished: List<ShoppingSession>,
        active: ShoppingSession?,
        stock: List<StockItem>,
    ): StartOverview {
        val now = clock.nowMillis()
        val newestFirst = finished.sortedByDescending { it.endedAt ?: it.startedAt }
        val monthKey = now.toMonthKey()
        val thisMonth = newestFirst.filter { (it.endedAt ?: it.startedAt).toMonthKey() == monthKey }
        val monthTotal = thisMonth.sumOf { session -> session.items.sumOf { it.price } }

        return StartOverview(
            greeting = Greeting.forHour(now.hourOfDay()),
            activeSession = active?.toSummary(),
            monthTotal = monthTotal,
            monthCount = thisMonth.size,
            monthAverage = if (thisMonth.isEmpty()) 0 else monthTotal / thisMonth.size,
            recent = newestFirst.take(RECENT_LIMIT).map { it.toSummary() },
            storeSuggestions = newestFirst.distinctStores(),
            hasAnySession = finished.isNotEmpty(),
        )
    }

    private fun ShoppingSession.toSummary(): SessionSummary = SessionSummary(
        session = this,
        total = items.sumOf { it.price },
        itemCount = items.size,
    )

    private fun List<ShoppingSession>.distinctStores(): List<String> = asSequence()
        .map { it.store.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.normalized() }
        .take(STORE_LIMIT)
        .toList()

    private fun Long.hourOfDay(): Int =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).hour
}
