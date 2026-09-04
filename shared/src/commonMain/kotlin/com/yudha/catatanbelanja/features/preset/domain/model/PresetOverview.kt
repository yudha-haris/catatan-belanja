package com.yudha.catatanbelanja.features.preset.domain.model

import com.yudha.catatanbelanja.core.domain.model.AppLanguage

/** The counts the preset hub prints under each row, plus the language it shows on the last one. */
data class PresetOverview(
    val itemCount: Int = 0,
    val categoryCount: Int = 0,
    val brandCount: Int = 0,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
