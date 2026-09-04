package com.yudha.catatanbelanja.features.shopping.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.shopping.domain.model.Greeting

data class StartState(
    val loadState: UiState<Unit> = UiState.Initial,
    val greeting: Greeting = Greeting.MORNING,
    val activeSession: SessionSummary? = null,
    val monthTotal: Int = 0,
    val monthCount: Int = 0,
    val monthAverage: Int = 0,
    val recent: List<SessionSummary> = emptyList(),
    val storeSuggestions: List<String> = emptyList(),
    val hasAnySession: Boolean = false,

    // The plan for the next trip. The card is a fixed slot on the screen: with no plan it
    // offers to start one, so the layout does not jump once the user makes their first list.
    val hasList: Boolean = false,
    val listTotalCount: Int = 0,
    val listRemainingCount: Int = 0,
    /** The first few things still to buy, for the card's one-line preview. */
    val listPreviewNames: List<String> = emptyList(),
    /** How many more there are beyond [listPreviewNames] — the card's "+4". */
    val listExtraCount: Int = 0,
)
