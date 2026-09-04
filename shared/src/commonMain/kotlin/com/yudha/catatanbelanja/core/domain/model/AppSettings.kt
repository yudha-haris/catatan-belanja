package com.yudha.catatanbelanja.core.domain.model

data class AppSettings(
    val themeFlavor: ThemeFlavor = ThemeFlavor.PURPLE,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
