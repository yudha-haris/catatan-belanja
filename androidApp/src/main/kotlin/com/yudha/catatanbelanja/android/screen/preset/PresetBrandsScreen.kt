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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.screen.preset.components.PresetBrandSheet
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.preset.presentation.PresetBrandsEffect
import com.yudha.catatanbelanja.features.preset.presentation.PresetBrandsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The "Merk" preset — one flat list. These chips are offered under every item, beside the
 * per-item ones the live session already builds out of past trips.
 */
@Composable
fun PresetBrandsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PresetBrandsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    val nameRequired = stringResource(R.string.preset_toast_name_required)
    val duplicate = stringResource(R.string.preset_brands_toast_duplicate)
    val saved = stringResource(R.string.common_saved)
    val deleted = stringResource(R.string.preset_brands_toast_deleted)

    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                PresetBrandsEffect.NameRequired -> appUi.showToast(nameRequired)
                PresetBrandsEffect.DuplicateName -> appUi.showToast(duplicate)
                PresetBrandsEffect.Saved -> appUi.showToast(saved)
                PresetBrandsEffect.Deleted -> {
                    pendingDeleteId = null
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
                title = stringResource(R.string.preset_brands_title),
                subtitle = stringResource(R.string.preset_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            AppButton(
                text = stringResource(R.string.preset_brands_add),
                onClick = { viewModel.openEditor() },
                emoji = "➕",
                big = true,
                enabled = !isBusy,
            )
        },
    ) {
        if (state.brands.isEmpty()) {
            AppEmptyState(
                emoji = "🏷️",
                title = stringResource(R.string.preset_brands_empty_title),
                message = stringResource(R.string.preset_brands_empty_message),
            )
        }

        state.brands.forEachIndexed { index, brand ->
            if (index > 0) Spacer(Modifier.height(Spacing.x8))
            AppListRow(
                emoji = "🏷️",
                title = brand.name,
                dense = true,
                onClick = { viewModel.openEditor(brand) },
            )
        }
    }

    if (state.isEditorOpen) {
        PresetBrandSheet(
            brand = state.editorBrand,
            enabled = !isBusy,
            onSave = viewModel::saveBrand,
            onDelete = { id -> pendingDeleteId = id },
            onDismiss = viewModel::closeEditor,
        )
    }

    val deleteId = pendingDeleteId ?: return

    ConfirmationBottomSheet(
        title = stringResource(R.string.preset_brands_delete_title),
        message = stringResource(R.string.preset_brands_delete_message),
        confirmText = stringResource(R.string.common_delete),
        onConfirm = { viewModel.deleteBrand(deleteId) },
        onDismiss = { pendingDeleteId = null },
        isDanger = true,
    )
}
