package com.yudha.catatanbelanja.features.history.domain.model

/** One "Agustus 2026" block of the history list: its sessions and what they cost together. */
data class HistoryMonthGroup(
    val monthKey: String,
    val total: Int,
    val summaries: List<HistorySessionRowView>,
)
