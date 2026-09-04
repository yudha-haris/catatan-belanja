package com.yudha.catatanbelanja.features.settings.presentation

import com.yudha.catatanbelanja.core.domain.model.ImportSummary
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

sealed interface SettingsEffect {
    /** The flavour is applied and saved; the screen toasts the message for it. */
    data class ThemeApplied(val flavor: ThemeFlavor) : SettingsEffect

    data object DemoSeeded : SettingsEffect

    data object ExportShared : SettingsEffect

    data object ExportCopied : SettingsEffect

    /** Merge finished. A summary with `sessionsAdded == 0` is the "Tidak ada sesi baru" case. */
    data class ImportMerged(val summary: ImportSummary) : SettingsEffect

    /** The pasted or picked text was not a readable backup — reported as bad JSON, not as a crash. */
    data object ImportRejected : SettingsEffect

    data object DataCleared : SettingsEffect

    /** Sent after anything that rewrites the database, so the shell can reload the other tabs. */
    data object DataChanged : SettingsEffect
}
