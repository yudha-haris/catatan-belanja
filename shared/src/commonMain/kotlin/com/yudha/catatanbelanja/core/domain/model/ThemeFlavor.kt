package com.yudha.catatanbelanja.core.domain.model

enum class ThemeFlavor(val storageValue: String) {
    PURPLE("purple"),
    GREEN("green"),
    BLUE("blue"),
    ;

    companion object {
        /** Maps a settings-row / backup-JSON value back to a flavour; unknown input falls back to [PURPLE]. */
        fun fromStorage(value: String): ThemeFlavor {
            val key = value.trim().lowercase()
            return entries.firstOrNull { it.storageValue == key } ?: PURPLE
        }
    }
}
