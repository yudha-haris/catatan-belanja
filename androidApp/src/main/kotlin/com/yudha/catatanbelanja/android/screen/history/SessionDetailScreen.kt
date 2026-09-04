package com.yudha.catatanbelanja.android.screen.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.yudha.catatanbelanja.android.designsystem.component.feedback.ConfirmationBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.screen.history.components.SessionDetailCompareSheet
import com.yudha.catatanbelanja.android.screen.history.components.SessionDetailContent
import com.yudha.catatanbelanja.android.screen.history.components.SessionDetailItemSheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow
import com.yudha.catatanbelanja.features.history.presentation.SessionDetailEffect
import com.yudha.catatanbelanja.features.history.presentation.SessionDetailViewModel
import org.koin.androidx.compose.koinViewModel

/** One finished session — the prototype's `detailView()`. Pushed route, no tab bar. */
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    onOpenCompare: (String, String) -> Unit,
    onOpenLiveSession: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val deletedMessage = stringResource(R.string.detail_toast_deleted)
    val itemSavedMessage = stringResource(R.string.common_item_toast_saved)
    val itemDeletedMessage = stringResource(R.string.common_item_toast_deleted)
    val sessionRunningMessage = stringResource(R.string.detail_toast_session_running)

    var editingRow by remember { mutableStateOf<SessionItemRow?>(null) }
    var showComparePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load(sessionId)
        viewModel.effects.collect { effect ->
            when (effect) {
                SessionDetailEffect.NotFound -> onBack()
                SessionDetailEffect.Deleted -> {
                    appUi.showToast(deletedMessage)
                    onBack()
                }
                SessionDetailEffect.ItemSaved -> appUi.showToast(itemSavedMessage)
                SessionDetailEffect.ItemDeleted -> appUi.showToast(itemDeletedMessage)
                is SessionDetailEffect.OpenCompare -> onOpenCompare(effect.aId, effect.bId)
                // The live session screen raises the "daftar sebelumnya…" hint once it is there.
                is SessionDetailEffect.OpenLiveSession -> onOpenLiveSession(effect.itemNames)
                SessionDetailEffect.ActiveSessionRunning -> appUi.showToast(sessionRunningMessage)
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
        if (action is UiState.Loading) {
            appUi.showLoading()
            return@LaunchedEffect
        }
        appUi.dismissLoading()
        if (action !is UiState.Error) return@LaunchedEffect
        appUi.showError(action.failure)
    }

    val summary = state.summary
    AppScaffold(
        modifier = modifier,
        scrollable = false,
        contentPadding = PaddingValues(),
    ) {
        if (summary == null) return@AppScaffold

        SessionDetailContent(
            summary = summary,
            state = state,
            onBack = onBack,
            onOpenComparePicker = { showComparePicker = true },
            onRepeatSession = viewModel::repeatSession,
            onItemClicked = { row -> editingRow = row },
            onDeleteSession = { showDeleteConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }

    val row = editingRow
    if (row != null) {
        SessionDetailItemSheet(
            item = row.item,
            emoji = row.emoji,
            onSave = { name, qtyText, unit, note, priceText ->
                editingRow = null
                viewModel.updateItem(
                    itemId = row.item.id,
                    name = name,
                    qtyText = qtyText,
                    unit = unit,
                    note = note,
                    priceText = priceText,
                )
            },
            onDelete = {
                editingRow = null
                viewModel.deleteItem(row.item.id)
            },
            onDismiss = { editingRow = null },
        )
    }

    if (showComparePicker) {
        SessionDetailCompareSheet(
            sessions = state.otherSessions,
            onPick = { otherId ->
                showComparePicker = false
                viewModel.compareWith(otherId)
            },
            onDismiss = { showComparePicker = false },
        )
    }

    if (!showDeleteConfirm) return

    ConfirmationBottomSheet(
        title = stringResource(R.string.detail_delete_sheet_title),
        message = stringResource(R.string.detail_delete_sheet_message),
        confirmText = stringResource(R.string.common_delete),
        onConfirm = {
            showDeleteConfirm = false
            viewModel.deleteSession()
        },
        onDismiss = { showDeleteConfirm = false },
        cancelText = stringResource(R.string.common_back),
        isDanger = true,
    )
}
