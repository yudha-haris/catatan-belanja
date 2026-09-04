package com.yudha.catatanbelanja.android.locale

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import java.util.Locale

/**
 * Draws [content] in [language], whatever the phone is set to.
 *
 * The app carries both string sets already (`values` is English, `values-in` Indonesian), so this
 * only has to point the resource lookup at the right one: a context built on an overridden
 * `Configuration`, handed down through [LocalContext] and [LocalConfiguration], which is what
 * `stringResource` reads. No activity recreation, so switching the language in Pengaturan
 * re-letters the screen under the user's finger instead of blinking the app away and back.
 */
@Composable
fun AppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val localized = remember(language, configuration) { context.localizedFor(language) }

    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        content = content,
    )
}

// AppBundleLocaleChanges wants Play Core's language downloads alongside a locale switch. Both
// string sets ship inside the APK — there is no bundle, no language split and nothing to
// download — so the check does not apply here.
@SuppressLint("AppBundleLocaleChanges")
private fun Context.localizedFor(language: AppLanguage): Context {
    val locale = language.languageTag?.let(Locale::forLanguageTag) ?: systemLocale()

    // `java.time` takes its day and month names from the process default, and DateFormat reads
    // that per call — so this is what keeps "Kam, 13 Agu" in the same language as the copy beside
    // it. It runs during composition, before anything below has had a chance to format a date.
    Locale.setDefault(locale)

    val localized = Configuration(resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    return createConfigurationContext(localized)
}

/**
 * What the *phone* is set to, which is not `Locale.getDefault()` any more once an explicit
 * language has been applied above. Reading the system resources is the only source that stays
 * honest about it, and it is what makes "Ikuti sistem" switchable back to.
 */
private fun systemLocale(): Locale = Resources.getSystem().configuration.locales[0]
