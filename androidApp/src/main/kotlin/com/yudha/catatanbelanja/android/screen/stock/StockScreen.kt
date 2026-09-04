package com.yudha.catatanbelanja.android.screen.stock

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadge
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.screen.stock.components.StockCheckCallout
import com.yudha.catatanbelanja.android.screen.stock.components.StockCheckSheet
import com.yudha.catatanbelanja.android.screen.stock.components.StockEditorSheet
import com.yudha.catatanbelanja.android.screen.stock.components.StockLogRow
import com.yudha.catatanbelanja.android.screen.stock.components.StockLogSheet
import com.yudha.catatanbelanja.android.screen.stock.components.StockRowItem
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.stock.presentation.StockEffect
import com.yudha.catatanbelanja.features.stock.presentation.StockViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun StockScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val context = LocalContext.current

    // The editor sheet titles itself with the tapped row's emoji; the state only carries the item.
    var editorEmoji by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                StockEffect.NameRequired ->
                    appUi.showToast(context.getString(R.string.stock_toast_name_required))
                StockEffect.ItemSaved ->
                    appUi.showToast(context.getString(R.string.stock_toast_saved))
                StockEffect.ItemDeleted ->
                    appUi.showToast(context.getString(R.string.stock_toast_deleted))
                is StockEffect.ItemMarkedEmpty ->
                    appUi.showToast(context.getString(R.string.stock_toast_marked_empty, effect.name))
                is StockEffect.CheckSaved -> {
                    appUi.celebrate()
                    appUi.showToast(
                        context.getString(
                            R.string.stock_check_toast_saved,
                            effect.month.monthKeyToLabel(),
                        ),
                    )
                }
                StockEffect.LogDeleted ->
                    appUi.showToast(context.getString(R.string.stock_log_toast_deleted))
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
        contentPadding = PaddingValues(0.dp),
        scrollable = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = AppTheme.shapes.screenPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            item(key = "header") {
                AppScreenHeader(
                    title = stringResource(R.string.stock_title),
                    subtitle = context.stockSubtitle(state.totalCount, state.lowCount),
                    actions = {
                        AppIconButton(
                            onClick = {
                                editorEmoji = ""
                                viewModel.openEditor(null)
                            },
                            contentDescription = stringResource(R.string.common_cd_add),
                            emoji = "＋",
                            tint = AppTheme.colors.primary,
                        )
                        AppIconButton(
                            onClick = onOpenSettings,
                            contentDescription = stringResource(R.string.common_cd_settings),
                            icon = Icons.Rounded.Settings,
                        )
                    },
                )
            }

            if (!state.hasAny) {
                item(key = "empty") {
                    AppEmptyState(
                        emoji = "📦",
                        title = stringResource(R.string.stock_empty_title),
                        message = stringResource(R.string.stock_empty_message),
                        action = {
                            AppButton(
                                text = stringResource(R.string.stock_empty_action),
                                onClick = {
                                    editorEmoji = ""
                                    viewModel.openEditor(null)
                                },
                                emoji = "＋",
                                fillWidth = false,
                            )
                        },
                    )
                }
            }

            if (state.lowRows.isNotEmpty()) {
                item(key = "lowHeader") {
                    AppSectionHeader(
                        title = stringResource(R.string.stock_section_low),
                        trailing = {
                            AppBadge(text = "${state.lowCount}", tone = AppBadgeTone.Up)
                        },
                    )
                }
                items(state.lowRows, key = { it.item.id }) { row ->
                    StockRowItem(
                        row = row,
                        onClick = {
                            editorEmoji = row.emoji
                            viewModel.openEditor(row.item.id)
                        },
                    )
                }
            }

            if (state.okRows.isNotEmpty()) {
                item(key = "okHeader") {
                    AppSectionHeader(
                        title = stringResource(R.string.stock_section_ok),
                        trailing = {
                            Text(
                                text = stringResource(R.string.stock_section_ok_hint),
                                style = AppTheme.typography.tiny,
                                color = AppTheme.colors.inkTertiary,
                            )
                        },
                    )
                }
                items(state.okRows, key = { it.item.id }) { row ->
                    StockRowItem(
                        row = row,
                        onClick = {
                            editorEmoji = row.emoji
                            viewModel.openEditor(row.item.id)
                        },
                    )
                }
            }

            if (state.hasAny) {
                item(key = "checkCta") {
                    StockCheckCallout(enabled = !isBusy, onClick = viewModel::openCheckSheet)
                }
            }

            if (state.logs.isNotEmpty()) {
                item(key = "logsHeader") {
                    AppSectionHeader(title = stringResource(R.string.stock_logs_title))
                }
                items(state.logs, key = { it.log.id }) { logView ->
                    StockLogRow(
                        view = logView,
                        onClick = { viewModel.openLog(logView.log.id) },
                    )
                }
            }
        }
    }

    if (state.isEditorOpen) {
        StockEditorSheet(
            isNew = state.isEditorNew,
            item = state.editorItem,
            emoji = editorEmoji,
            unit = state.editorUnit,
            units = state.units,
            knownNames = state.knownNames,
            enabled = !isBusy,
            onNameChanged = viewModel::onEditorNameChanged,
            onSave = viewModel::saveStockItem,
            onMarkEmpty = viewModel::markEditorItemEmpty,
            onDelete = viewModel::deleteEditorItem,
            onDismiss = viewModel::closeEditor,
        )
    }

    if (state.isCheckOpen) {
        StockCheckSheet(
            rows = state.checkRows,
            checkedAtMillis = state.checkedAtMillis,
            enabled = !isBusy,
            onSave = viewModel::saveStockCheck,
            onDismiss = viewModel::closeCheckSheet,
        )
    }

    val logDetail = state.logDetail ?: return

    StockLogSheet(
        view = logDetail,
        usageRows = state.usageRows,
        previousMonth = state.usagePreviousMonth,
        enabled = !isBusy,
        onDelete = { viewModel.deleteLog(logDetail.log.id) },
        onDismiss = viewModel::closeLog,
    )
}

/** "12 barang", plus the prototype's "· 3 perlu dibeli" tail once something needs buying. */
private fun Context.stockSubtitle(total: Int, low: Int): String {
    val items = resources.getQuantityString(R.plurals.common_item_count, total, total)
    if (low == 0) return items
    val dot = getString(R.string.common_separator_dot)
    return "$items $dot ${getString(R.string.stock_subtitle_low, low)}"
}
