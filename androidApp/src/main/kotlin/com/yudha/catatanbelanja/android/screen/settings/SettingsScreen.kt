package com.yudha.catatanbelanja.android.screen.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsDataCard
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsExportSheet
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsImportSheet
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsStorageCard
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsThemePicker
import com.yudha.catatanbelanja.android.screen.settings.components.SettingsTipsCard
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.ImportSummary
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import com.yudha.catatanbelanja.features.settings.presentation.SettingsEffect
import com.yudha.catatanbelanja.features.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

/** Some file managers hand a JSON backup over as plain text, so both types are accepted. */
private val ImportMimeTypes = arrayOf("application/json", "text/plain")

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showExportSheet by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showClearSheet by remember { mutableStateOf(false) }

    val pickBackupFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val raw = withContext(Dispatchers.IO) { readTextOrNull(context, uri) }
            if (raw == null) {
                appUi.showToast(context.getString(R.string.settings_import_toast_file_unreadable))
                return@launch
            }
            viewModel.importFromText(raw)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ThemeApplied -> appUi.showToast(context.themeToast(effect.flavor))
                SettingsEffect.DemoSeeded -> appUi.showToast(context.getString(R.string.common_demo_added))
                // The system share sheet is its own confirmation — a toast on top of it just nags.
                SettingsEffect.ExportShared -> showExportSheet = false
                SettingsEffect.ExportCopied -> {
                    showExportSheet = false
                    appUi.showToast(context.getString(R.string.settings_export_toast_copied))
                }
                is SettingsEffect.ImportMerged -> {
                    showImportSheet = false
                    appUi.showToast(context.mergeToast(effect.summary))
                }
                // The sheet stays open: the user still has the bad paste in front of them to fix.
                SettingsEffect.ImportRejected ->
                    appUi.showToast(context.getString(R.string.settings_import_toast_invalid))
                SettingsEffect.DataCleared -> {
                    showClearSheet = false
                    appUi.showToast(context.getString(R.string.settings_clear_toast))
                }
                // The shell reloads the tabs; nothing on this screen reacts to it.
                SettingsEffect.DataChanged -> Unit
            }
        }
    }

    LaunchedEffect(state.loadState) {
        val load = state.loadState
        if (load !is UiState.Error) return@LaunchedEffect
        appUi.showError(load.failure)
    }

    LaunchedEffect(state.actionState) {
        val action = state.actionState
        if (action !is UiState.Error) return@LaunchedEffect
        appUi.showError(action.failure)
    }

    val isBusy = state.actionState is UiState.Loading

    AppScaffold(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 32.dp),
        header = {
            AppScreenHeader(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                onBack = onBack,
            )
        },
    ) {
        AppSectionHeader(title = stringResource(R.string.settings_theme_title))
        SettingsThemePicker(
            selected = state.themeFlavor,
            onSelect = viewModel::changeTheme,
        )

        AppSectionHeader(title = stringResource(R.string.settings_data_title))
        SettingsDataCard(
            sessionCount = state.sessionCount,
            stockCount = state.stockCount,
            enabled = !isBusy,
            onSeedDemo = viewModel::seedDemo,
            onExport = { showExportSheet = true },
            onImport = { showImportSheet = true },
            onClearAll = { showClearSheet = true },
        )

        Spacer(Modifier.height(Spacing.x16))
        SettingsStorageCard()

        Spacer(Modifier.height(Spacing.x12))
        SettingsTipsCard()

        Spacer(Modifier.height(Spacing.x12))
        Text(
            text = stringResource(R.string.settings_footer),
            style = AppTheme.typography.tiny,
            color = AppTheme.colors.inkTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showExportSheet) {
        SettingsExportSheet(
            enabled = !isBusy,
            onShare = viewModel::exportShare,
            onCopy = viewModel::exportCopy,
            onDismiss = { showExportSheet = false },
        )
    }

    if (showImportSheet) {
        SettingsImportSheet(
            enabled = !isBusy,
            onPickFile = { pickBackupFile.launch(ImportMimeTypes) },
            onSubmit = viewModel::importFromText,
            onDismiss = { showImportSheet = false },
        )
    }

    if (!showClearSheet) return

    ConfirmationBottomSheet(
        title = stringResource(R.string.settings_clear_sheet_title),
        message = pluralStringResource(
            R.plurals.settings_clear_sheet_message,
            state.sessionCount,
            state.sessionCount,
        ),
        confirmText = stringResource(R.string.settings_clear_confirm),
        cancelText = stringResource(R.string.common_back),
        onConfirm = viewModel::clearAll,
        onDismiss = { showClearSheet = false },
        isDanger = true,
    )
}

private fun Context.themeToast(flavor: ThemeFlavor): String = when (flavor) {
    ThemeFlavor.PURPLE -> getString(R.string.settings_theme_toast_purple)
    ThemeFlavor.GREEN -> getString(R.string.settings_theme_toast_green)
    ThemeFlavor.BLUE -> getString(R.string.settings_theme_toast_blue)
}

/**
 * A merge that added no session is the prototype's "Tidak ada sesi baru", not a failure — but a
 * document that carried only shopping lists did add something, so it says so instead.
 */
private fun Context.mergeToast(summary: ImportSummary): String {
    if (summary.sessionsAdded > 0) {
        return resources.getQuantityString(
            R.plurals.settings_import_toast_merged,
            summary.sessionsAdded,
            summary.sessionsAdded,
        )
    }
    if (summary.listsAdded > 0) {
        return resources.getQuantityString(
            R.plurals.settings_import_toast_lists,
            summary.listsAdded,
            summary.listsAdded,
        )
    }
    return getString(R.string.settings_import_toast_none)
}

private fun readTextOrNull(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
}.getOrNull()
