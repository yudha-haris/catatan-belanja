package com.yudha.catatanbelanja.features.settings.domain.model

import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

/** What the settings screen needs on load: the active theme plus the counts behind "Hapus semua data". */
data class SettingsOverview(
    val themeFlavor: ThemeFlavor = ThemeFlavor.PURPLE,
    val sessionCount: Int = 0,
    val stockCount: Int = 0,
)
