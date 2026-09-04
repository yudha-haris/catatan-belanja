package com.yudha.catatanbelanja.android.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.list.components.ListAddCard
import com.yudha.catatanbelanja.android.screen.list.components.ListItemRow
import com.yudha.catatanbelanja.android.screen.list.components.ListMenuSheet
import com.yudha.catatanbelanja.android.screen.list.components.ListProgressCard
import com.yudha.catatanbelanja.android.screen.list.components.ListSourceSheet
import com.yudha.catatanbelanja.android.screen.list.components.ListTemplateSheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.list.domain.model.ListSource
import com.yudha.catatanbelanja.features.list.presentation.ShoppingListEffect
import com.yudha.catatanbelanja.features.list.presentation.ShoppingListViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Daftar belanja — the plan for the next trip.
 *
 * Two screens in one: with no plan it is the "buat daftar" menu, with one it is a checklist.
 * A pushed route, so it never draws the tab bar.
 */
@Composable
fun ShoppingListScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val context = LocalContext.current
    val addFocus = remember { FocusRequester() }

    var showSourceSheet by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    // Confirming a template deletion needs the source sheet out of the way; it comes back
    // either way, so the user lands where they were.
    var deletingTemplate by remember { mutableStateOf<ListSource?>(null) }
    // Arriving with nothing planned, the menu is the screen — but only the first time, so
    // deleting a list does not trap the user in a sheet they just dismissed.
    var didOfferSources by rememberSaveable { mutableStateOf(false) }

    val alreadyMessage = stringResource(R.string.list_toast_already)
    val needNameMessage = stringResource(R.string.list_toast_need_name)
    val completeMessage = stringResource(R.string.list_toast_complete)
    val templateSavedMessage = stringResource(R.string.list_toast_template_saved)
    val deletedMessage = stringResource(R.string.list_toast_deleted)
    val startedBlankMessage = stringResource(R.string.list_toast_started_blank)
    val templateDeletedMessage = stringResource(R.string.list_toast_template_deleted)

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                is ShoppingListEffect.ListStarted -> {
                    showSourceSheet = false
                    val toast = when (effect.itemCount) {
                        0 -> startedBlankMessage
                        else -> context.resources.getQuantityString(
                            R.plurals.list_toast_started,
                            effect.itemCount,
                            effect.itemCount,
                        )
                    }
                    appUi.showToast(toast)
                }

                ShoppingListEffect.ListCompleted -> {
                    appUi.celebrate()
                    appUi.showToast(completeMessage)
                }

                ShoppingListEffect.TemplateSaved -> {
                    showTemplateSheet = false
                    appUi.showToast(templateSavedMessage)
                }

                ShoppingListEffect.TemplateDeleted -> {
                    deletingTemplate = null
                    showSourceSheet = true
                    appUi.showToast(templateDeletedMessage)
                }

                ShoppingListEffect.ListDeleted -> {
                    showDeleteSheet = false
                    appUi.showToast(deletedMessage)
                    showSourceSheet = true
                }

                is ShoppingListEffect.ShowMessage -> when (effect.kind) {
                    ShoppingListEffect.Message.NAME_REQUIRED -> appUi.showToast(needNameMessage)
                    ShoppingListEffect.Message.ALREADY_ON_LIST -> appUi.showToast(alreadyMessage)
                }

                is ShoppingListEffect.ShowError -> appUi.showError(effect.failure)
            }
        }
    }

    // A list with nothing on it yet has exactly one sensible next action, so the keyboard opens
    // itself. Keyed on the card being on screen, so the requester is always attached by now.
    val isBlankList = state.hasList && state.totalCount == 0
    LaunchedEffect(isBlankList) {
        if (!isBlankList) return@LaunchedEffect

        addFocus.requestFocus()
    }

    LaunchedEffect(state.loadState, state.hasList) {
        if (didOfferSources) return@LaunchedEffect
        if (state.loadState !is UiState.Success) return@LaunchedEffect
        if (state.hasList) return@LaunchedEffect

        didOfferSources = true
        showSourceSheet = true
    }

    AppScaffold(
        modifier = modifier,
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = AppTheme.shapes.screenPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            item(key = "header") {
                AppScreenHeader(
                    title = stringResource(R.string.list_title),
                    subtitle = when (state.totalCount) {
                        0 -> stringResource(R.string.list_subtitle_empty)
                        else -> stringResource(
                            R.string.list_subtitle_progress,
                            state.checkedCount,
                            state.totalCount,
                        )
                    },
                    onBack = onBack,
                    actions = {
                        if (!state.hasList) return@AppScreenHeader
                        AppIconButton(
                            onClick = { showMenuSheet = true },
                            contentDescription = stringResource(R.string.list_menu_cd),
                            emoji = "⋯",
                        )
                    },
                )
            }

            if (!state.hasList) {
                item(key = "empty") {
                    AppEmptyState(
                        emoji = "📝",
                        title = stringResource(R.string.list_empty_title),
                        message = stringResource(R.string.list_empty_message),
                        action = {
                            AppButton(
                                text = stringResource(R.string.list_empty_action),
                                onClick = { showSourceSheet = true },
                                emoji = "📝",
                                fillWidth = false,
                            )
                        },
                    )
                }
                return@LazyColumn
            }

            if (state.totalCount > 0) {
                item(key = "progress") {
                    ListProgressCard(
                        checkedCount = state.checkedCount,
                        totalCount = state.totalCount,
                        remainingCount = state.remainingCount,
                        progress = state.progress,
                        isComplete = state.isComplete,
                    )
                }
            }

            item(key = "add") {
                ListAddCard(
                    state = state,
                    focusRequester = addFocus,
                    onQueryChanged = viewModel::onQueryChanged,
                    onSubmit = viewModel::addTyped,
                    onPickName = viewModel::addName,
                )
            }

            item(key = "listHeader") {
                AppSectionHeader(title = stringResource(R.string.list_section_title))
            }

            items(items = state.itemViews, key = { it.item.id }) { view ->
                ListItemRow(
                    view = view,
                    onToggle = { viewModel.toggleItem(view.item.id, !view.item.isChecked) },
                    onRemove = { viewModel.removeItem(view.item.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (showSourceSheet) {
        ListSourceSheet(
            sources = state.sources,
            onPick = viewModel::startList,
            onDeleteTemplate = { source ->
                showSourceSheet = false
                deletingTemplate = source
            },
            onDismiss = { showSourceSheet = false },
        )
    }

    val templateToDelete = deletingTemplate
    if (templateToDelete != null) {
        ConfirmationBottomSheet(
            title = stringResource(R.string.list_template_delete_title, templateToDelete.label),
            message = stringResource(R.string.list_template_delete_message),
            confirmText = stringResource(R.string.list_delete_confirm),
            onConfirm = { templateToDelete.templateId?.let(viewModel::deleteTemplate) },
            onDismiss = {
                deletingTemplate = null
                showSourceSheet = true
            },
            isDanger = true,
        )
    }

    if (showMenuSheet) {
        ListMenuSheet(
            onSaveTemplate = {
                showMenuSheet = false
                showTemplateSheet = true
            },
            onDelete = {
                showMenuSheet = false
                showDeleteSheet = true
            },
            onDismiss = { showMenuSheet = false },
        )
    }

    if (showTemplateSheet) {
        ListTemplateSheet(
            defaultName = stringResource(R.string.list_template_name_placeholder),
            onSave = viewModel::saveAsTemplate,
            onDismiss = { showTemplateSheet = false },
        )
    }

    if (!showDeleteSheet) return

    ConfirmationBottomSheet(
        title = stringResource(R.string.list_delete_title),
        message = stringResource(R.string.list_delete_message),
        confirmText = stringResource(R.string.list_delete_confirm),
        onConfirm = viewModel::deleteList,
        onDismiss = { showDeleteSheet = false },
        isDanger = true,
    )
}
