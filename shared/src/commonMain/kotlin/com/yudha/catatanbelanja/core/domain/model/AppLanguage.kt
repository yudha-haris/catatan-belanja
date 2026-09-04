package com.yudha.catatanbelanja.core.domain.model

/**
 * The language the UI is drawn in. [SYSTEM] is the default and means "whatever the phone is set
 * to" — the app ships Indonesian (`values-in`) and English (`values`), so on any other device
 * language Android already falls back to English on its own.
 *
 * [languageTag] is a BCP 47 tag the platform layer turns into a locale; [SYSTEM] has none.
 */
enum class AppLanguage(val storageValue: String, val languageTag: String?) {
    SYSTEM("system", null),
    INDONESIAN("id", "id"),
    ENGLISH("en", "en"),
    ;

    companion object {
        /** Maps a settings-row value back to a language; unknown input falls back to [SYSTEM]. */
        fun fromStorage(value: String): AppLanguage {
            val key = value.trim().lowercase()
            return entries.firstOrNull { it.storageValue == key } ?: SYSTEM
        }
    }
}
