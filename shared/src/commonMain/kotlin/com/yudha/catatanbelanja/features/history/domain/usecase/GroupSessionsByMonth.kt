package com.yudha.catatanbelanja.features.history.domain.usecase

import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.features.history.domain.model.HistoryMonthGroup

/** Finished sessions, newest first, cut into month blocks — newest month block first. */
class GroupSessionsByMonth(private val buildSessionRowView: BuildSessionRowView) {
    operator fun invoke(sessions: List<ShoppingSession>): List<HistoryMonthGroup> {
        val summaries = sessions
            .filter { it.endedAt != null }
            .sortedByDescending { it.endedAt }
            .map(buildSessionRowView::invoke)
        if (summaries.isEmpty()) return emptyList()

        return summaries
            .groupBy { (it.summary.session.endedAt ?: 0L).toMonthKey() }
            .map { (monthKey, monthSummaries) ->
                HistoryMonthGroup(
                    monthKey = monthKey,
                    total = monthSummaries.sumOf { it.summary.total },
                    summaries = monthSummaries,
                )
            }
            .sortedByDescending { it.monthKey }
    }
}
