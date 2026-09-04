package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.AppLanguage

/** The hub only counts things; every edit happens one screen deeper. */
data class PresetHubState(
    val loadState: UiState<Unit> = UiState.Initial,
    val itemCount: Int = 0,
    val categoryCount: Int = 0,
    val brandCount: Int = 0,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
