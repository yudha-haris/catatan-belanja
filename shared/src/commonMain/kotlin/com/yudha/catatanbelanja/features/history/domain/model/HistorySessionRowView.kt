package com.yudha.catatanbelanja.features.history.domain.model

import com.yudha.catatanbelanja.core.domain.model.SessionSummary

/**
 * One finished session as the history list and the "bandingkan dengan…" sheet draw it: the
 * summary plus every label decision already made for the row.
 */
data class HistorySessionRowView(
    val summary: SessionSummary,
    /** The store only earns its own slot when it is not simply the session name again. */
    val showStore: Boolean,
)
