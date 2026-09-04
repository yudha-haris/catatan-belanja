package com.yudha.catatanbelanja.features.history.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.features.history.domain.model.HistorySessionRowView

/** The row shape shared by the history list and the "bandingkan dengan…" sheet. */
class BuildSessionRowView {
    operator fun invoke(session: ShoppingSession): HistorySessionRowView = HistorySessionRowView(
        summary = SessionSummary(
            session = session,
            total = session.items.sumOf { it.price },
            itemCount = session.items.size,
        ),
        showStore = session.store.isNotBlank() &&
            session.store.normalized() != session.name.normalized(),
    )
}
