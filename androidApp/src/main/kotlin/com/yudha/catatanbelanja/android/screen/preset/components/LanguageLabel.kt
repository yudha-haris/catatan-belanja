package com.yudha.catatanbelanja.android.screen.preset.components

import androidx.annotation.StringRes
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.core.domain.model.AppLanguage

/**
 * The label for a language. It lives here rather than in a view model for the usual reason: the
 * view model emits the enum, the composable resolves the words.
 */
@StringRes
internal fun AppLanguage.languageLabel(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.preset_language_system
    AppLanguage.INDONESIAN -> R.string.preset_language_indonesian
    AppLanguage.ENGLISH -> R.string.preset_language_english
}
