package com.yudha.catatanbelanja.android.screen.preset

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.preset.components.PresetCategorySheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.features.preset.presentation.PresetCategoriesEffect
import com.yudha.catatanbelanja.features.preset.presentation.PresetCategoriesViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The "Kategori" preset. Deleting one takes every item filed under it, so the confirmation quotes
 * that count rather than asking a generic "are you sure".
 */
@Composable
fun PresetCategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetCategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    val nameRequired = stringResource(R.string.preset_toast_name_required)
    val duplicate = stringResource(R.string.preset_categories_toast_duplicate)
    val saved = stringResource(R.string.common_saved)
    val deleted = stringResource(R.string.preset_categories_toast_deleted)
    val restored = stringResource(R.string.preset_categories_toast_restored)

    var pendingDelete by remember { mutableStateOf<CatalogCategory?>(null) }
    var showResetSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                PresetCategoriesEffect.NameRequired -> appUi.showToast(nameRequired)
                PresetCategoriesEffect.DuplicateName -> appUi.showToast(duplicate)
                PresetCategoriesEffect.Saved -> appUi.showToast(saved)
                PresetCategoriesEffect.Deleted -> {
                    pendingDelete = null
                    appUi.showToast(deleted)
                }
                PresetCategoriesEffect.ResetToDefaults -> {
                    showResetSheet = false
                    appUi.showToast(restored)
                }
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
        header = {
            AppScreenHeader(
                title = stringResource(R.string.preset_categories_title),
                subtitle = stringResource(R.string.preset_title),
                onBack = onBack,
                actions = {
                    AppIconButton(
                        onClick = { showResetSheet = true },
                        contentDescription = stringResource(R.string.preset_categories_reset),
                        emoji = "♻️",
                    )
                },
            )
        },
        bottomBar = {
            AppButton(
                text = stringResource(R.string.preset_categories_add),
                onClick = { viewModel.openEditor() },
                emoji = "➕",
                big = true,
                enabled = !isBusy,
            )
        },
    ) {
        if (state.categories.isEmpty()) {
            AppEmptyState(
                emoji = "🗂️",
                title = stringResource(R.string.preset_categories_empty_title),
                message = stringResource(R.string.preset_categories_empty_message),
            )
        }

        state.categories.forEachIndexed { index, category ->
            if (index > 0) Spacer(Modifier.height(Spacing.x8))
            AppListRow(
                emoji = category.emoji,
                title = category.name,
                subtitle = pluralStringResource(
                    R.plurals.preset_item_count,
                    category.items.size,
                    category.items.size,
                ),
                dense = true,
                onClick = { viewModel.openEditor(category) },
            )
        }
    }

    if (state.isEditorOpen) {
        PresetCategorySheet(
            category = state.editorCategory,
            enabled = !isBusy,
            onSave = viewModel::saveCategory,
            onDelete = { category -> pendingDelete = category },
            onDismiss = viewModel::closeEditor,
        )
    }

    if (showResetSheet) {
        ConfirmationBottomSheet(
            title = stringResource(R.string.preset_categories_reset_title),
            message = stringResource(R.string.preset_categories_reset_message),
            confirmText = stringResource(R.string.preset_categories_reset_confirm),
            cancelText = stringResource(R.string.common_cancel),
            onConfirm = viewModel::resetToDefaults,
            onDismiss = { showResetSheet = false },
            isDanger = true,
        )
    }

    val target = pendingDelete ?: return

    ConfirmationBottomSheet(
        title = stringResource(R.string.preset_categories_delete_title, target.name),
        message = pluralStringResource(
            R.plurals.preset_categories_delete_message,
            target.items.size,
            target.items.size,
        ),
        confirmText = stringResource(R.string.common_delete),
        onConfirm = { viewModel.deleteCategory(target.id) },
        onDismiss = { pendingDelete = null },
        isDanger = true,
    )
}
