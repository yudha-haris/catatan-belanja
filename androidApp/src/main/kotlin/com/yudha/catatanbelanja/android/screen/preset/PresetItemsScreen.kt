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
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.input.AppSearchField
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.preset.components.PresetItemSheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.CatalogItem
import com.yudha.catatanbelanja.features.preset.presentation.PresetItemsEffect
import com.yudha.catatanbelanja.features.preset.presentation.PresetItemsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The "Belanjaan" preset: every catalog item, under its category and searchable. These are the
 * names the add form offers before a single trip has been recorded.
 */
@Composable
fun PresetItemsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetItemsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    val nameRequired = stringResource(R.string.preset_toast_name_required)
    val categoryRequired = stringResource(R.string.preset_items_toast_category_required)
    val duplicate = stringResource(R.string.preset_items_toast_duplicate)
    val saved = stringResource(R.string.common_saved)
    val deleted = stringResource(R.string.preset_items_toast_deleted)

    var pendingDelete by remember { mutableStateOf<CatalogItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                PresetItemsEffect.NameRequired -> appUi.showToast(nameRequired)
                PresetItemsEffect.CategoryRequired -> appUi.showToast(categoryRequired)
                PresetItemsEffect.DuplicateName -> appUi.showToast(duplicate)
                PresetItemsEffect.Saved -> appUi.showToast(saved)
                PresetItemsEffect.Deleted -> {
                    pendingDelete = null
                    appUi.showToast(deleted)
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
                title = stringResource(R.string.preset_items_title),
                subtitle = pluralStringResource(
                    R.plurals.preset_item_count,
                    state.totalCount,
                    state.totalCount,
                ),
                onBack = onBack,
            )
        },
        bottomBar = {
            AppButton(
                text = stringResource(R.string.preset_items_add),
                onClick = { viewModel.openEditor() },
                emoji = "➕",
                big = true,
                enabled = !isBusy && state.categories.isNotEmpty(),
            )
        },
    ) {
        AppSearchField(
            value = state.query,
            onValueChange = viewModel::onQueryChanged,
            onClear = { viewModel.onQueryChanged("") },
            placeholder = stringResource(R.string.preset_items_search_placeholder),
        )

        if (state.categories.isEmpty()) {
            AppEmptyState(
                emoji = "🗂️",
                title = stringResource(R.string.preset_items_no_category_title),
                message = stringResource(R.string.preset_items_no_category_message),
            )
        }

        if (state.isSearchEmpty) {
            AppEmptyState(
                emoji = "🔍",
                title = stringResource(R.string.preset_items_search_empty_title),
                message = stringResource(R.string.preset_items_search_empty_message),
            )
        }

        state.sections.forEach { section ->
            AppSectionHeader(title = "${section.emoji} ${section.categoryName}")

            section.items.forEachIndexed { index, item ->
                if (index > 0) Spacer(Modifier.height(Spacing.x8))
                AppListRow(
                    title = item.name,
                    subtitle = item.defaultUnit.ifEmpty {
                        stringResource(R.string.preset_items_no_default_unit)
                    },
                    dense = true,
                    onClick = { viewModel.openEditor(item, section.categoryId) },
                )
            }
        }
    }

    if (state.isEditorOpen) {
        PresetItemSheet(
            item = state.editorItem,
            categories = state.categories,
            selectedCategoryId = state.editorCategoryId,
            unit = state.editorUnit,
            units = state.units,
            enabled = !isBusy,
            onPickCategory = viewModel::pickEditorCategory,
            onPickUnit = viewModel::pickEditorUnit,
            onSave = viewModel::saveItem,
            onDelete = { item -> pendingDelete = item },
            onDismiss = viewModel::closeEditor,
        )
    }

    val target = pendingDelete ?: return

    ConfirmationBottomSheet(
        title = stringResource(R.string.preset_items_delete_title, target.name),
        message = stringResource(R.string.preset_items_delete_message),
        confirmText = stringResource(R.string.common_delete),
        onConfirm = { viewModel.deleteItem(target.id) },
        onDismiss = { pendingDelete = null },
        isDanger = true,
    )
}
