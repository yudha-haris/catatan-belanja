package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadge
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.history.presentation.HistoryState

private const val COMPARE_EMOJI = "⇄"

/** Header, optional pick hint, then one section per month — the body of `riwayatView()`. */
@Composable
internal fun HistorySessionList(
    state: HistoryState,
    onOpenSettings: () -> Unit,
    onSessionClicked: (String) -> Unit,
    onToggleCompareMode: () -> Unit,
    onQuickCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = AppTheme.shapes.screenPadding,
    ) {
        item(key = "header") {
            AppScreenHeader(
                title = stringResource(R.string.history_title),
                subtitle = pluralStringResource(
                    R.plurals.history_session_count,
                    state.sessionCount,
                    state.sessionCount,
                ),
                actions = {
                    AppButton(
                        text = when (state.compareMode) {
                            true -> stringResource(R.string.history_compare_cancel)
                            false -> stringResource(R.string.history_compare_cta)
                        },
                        onClick = onToggleCompareMode,
                        variant = AppButtonVariant.Ghost,
                        emoji = COMPARE_EMOJI.takeIf { !state.compareMode },
                        fillWidth = false,
                    )
                    AppIconButton(
                        onClick = onOpenSettings,
                        contentDescription = stringResource(R.string.common_cd_settings),
                        icon = Icons.Rounded.Settings,
                    )
                },
            )
        }

        if (state.compareMode) {
            item(key = "compare-hint") {
                HistoryCompareHint(
                    pickedCount = state.pickedCount,
                    canQuickCompare = state.canQuickCompare,
                    onQuickCompare = onQuickCompare,
                )
            }
        }

        state.groups.forEach { group ->
            item(key = "month-${group.monthKey}") {
                AppSectionHeader(
                    title = group.monthKey.monthKeyToLabel(),
                    trailing = { AppBadge(text = group.total.toRupiahShort()) },
                )
            }

            items(items = group.summaries, key = { it.summary.session.id }) { view ->
                HistorySessionRow(
                    view = view,
                    compareMode = state.compareMode,
                    picked = state.pickedIds.contains(view.summary.session.id),
                    onClick = { onSessionClicked(view.summary.session.id) },
                    modifier = Modifier.padding(bottom = Spacing.x8),
                )
            }
        }
    }
}
