package com.yudha.catatanbelanja.android.screen.preset

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.preset.components.PresetLanguageNote
import com.yudha.catatanbelanja.android.screen.preset.components.languageLabel
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import com.yudha.catatanbelanja.features.preset.presentation.PresetLanguageEffect
import com.yudha.catatanbelanja.features.preset.presentation.PresetLanguageViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The "Bahasa" preset. Picking one saves it and the shell re-draws in it immediately — including
 * this screen, which is why the toast is read in the language just chosen.
 */
@Composable
fun PresetLanguageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetLanguageViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val appliedMessage = stringResource(R.string.preset_language_toast_applied)

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                is PresetLanguageEffect.LanguageApplied -> appUi.showToast(appliedMessage)
            }
        }
    }

    LaunchedEffect(state.loadState) {
        val load = state.loadState
        if (load !is UiState.Error) return@LaunchedEffect
        appUi.showError(load.failure)
    }

    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.preset_language_title),
                subtitle = stringResource(R.string.preset_title),
                onBack = onBack,
            )
        },
    ) {
        AppLanguage.entries.forEachIndexed { index, language ->
            if (index > 0) Spacer(Modifier.height(Spacing.x10))
            AppListRow(
                emoji = language.flagEmoji(),
                title = stringResource(language.languageLabel()),
                subtitle = stringResource(language.exampleLabel()),
                selected = language == state.language,
                onClick = { viewModel.changeLanguage(language) },
            )
        }

        Spacer(Modifier.height(Spacing.x16))
        PresetLanguageNote()
    }
}

/** Icons, not copy — the same category as the emoji tiles everywhere else in the app. */
private fun AppLanguage.flagEmoji(): String = when (this) {
    AppLanguage.SYSTEM -> "📱"
    AppLanguage.INDONESIAN -> "🇮🇩"
    AppLanguage.ENGLISH -> "🇬🇧"
}

private fun AppLanguage.exampleLabel(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.preset_language_system_example
    AppLanguage.INDONESIAN -> R.string.preset_language_indonesian_example
    AppLanguage.ENGLISH -> R.string.preset_language_english_example
}
