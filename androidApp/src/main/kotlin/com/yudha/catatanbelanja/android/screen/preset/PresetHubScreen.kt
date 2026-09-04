package com.yudha.catatanbelanja.android.screen.preset

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.preset.components.languageLabel
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.preset.presentation.PresetHubViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Pengaturan > Preset: what the app offers you before you have typed anything. Four lists, each
 * on its own screen — the counts here are the only thing this screen owns.
 */
@Composable
fun PresetHubScreen(
    onBack: () -> Unit,
    onOpenItems: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenBrands: () -> Unit,
    onOpenLanguage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetHubViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    // Re-reads on every return from a sub-screen, so an edit made there shows up in the counts.
    LaunchedEffect(Unit) {
        viewModel.load()
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
                title = stringResource(R.string.preset_title),
                subtitle = stringResource(R.string.settings_title),
                onBack = onBack,
            )
        },
    ) {
        AppSectionHeader(title = stringResource(R.string.preset_section_catalog))

        AppListRow(
            emoji = "🛒",
            title = stringResource(R.string.preset_items_title),
            subtitle = stringResource(R.string.preset_items_subtitle),
            trailing = pluralStringResource(
                R.plurals.preset_item_count,
                state.itemCount,
                state.itemCount,
            ),
            onClick = onOpenItems,
        )

        Spacer(Modifier.height(Spacing.x10))
        AppListRow(
            emoji = "🗂️",
            title = stringResource(R.string.preset_categories_title),
            subtitle = stringResource(R.string.preset_categories_subtitle),
            trailing = pluralStringResource(
                R.plurals.preset_category_count,
                state.categoryCount,
                state.categoryCount,
            ),
            onClick = onOpenCategories,
        )

        Spacer(Modifier.height(Spacing.x10))
        AppListRow(
            emoji = "🏷️",
            title = stringResource(R.string.preset_brands_title),
            subtitle = stringResource(R.string.preset_brands_subtitle),
            trailing = pluralStringResource(
                R.plurals.preset_brand_count,
                state.brandCount,
                state.brandCount,
            ),
            onClick = onOpenBrands,
        )

        AppSectionHeader(title = stringResource(R.string.preset_section_display))

        AppListRow(
            emoji = "🌐",
            title = stringResource(R.string.preset_language_title),
            subtitle = stringResource(R.string.preset_language_subtitle),
            trailing = stringResource(state.language.languageLabel()),
            onClick = onOpenLanguage,
        )
    }
}
