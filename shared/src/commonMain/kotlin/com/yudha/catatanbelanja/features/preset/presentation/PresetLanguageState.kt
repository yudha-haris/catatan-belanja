package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.AppLanguage

data class PresetLanguageState(
    val loadState: UiState<Unit> = UiState.Initial,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
