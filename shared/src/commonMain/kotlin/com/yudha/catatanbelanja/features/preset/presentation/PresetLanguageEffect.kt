package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.domain.model.AppLanguage

sealed interface PresetLanguageEffect {
    /** Saved and applied; the screen toasts it — in the language just chosen. */
    data class LanguageApplied(val language: AppLanguage) : PresetLanguageEffect
}
