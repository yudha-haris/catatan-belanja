package com.yudha.catatanbelanja.features.shopping.domain.model

import com.yudha.catatanbelanja.core.domain.model.SessionSummary

/** Everything the start screen shows, aggregated in one pass by `BuildStartOverview`. */
data class StartOverview(
    val greeting: Greeting,
    val activeSession: SessionSummary?,
    val monthTotal: Int,
    val monthCount: Int,
    val monthAverage: Int,
    val recent: List<SessionSummary>,
    val storeSuggestions: List<String>,
    val hasAnySession: Boolean,
)
