package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.history.domain.model.HistorySessionRowView

/** The "Bandingkan dengan…" picker — every other finished session, newest first. */
@Composable
internal fun SessionDetailCompareSheet(
    sessions: List<HistorySessionRowView>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.detail_compare_with_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            sessions.forEach { view ->
                HistorySessionRow(
                    view = view,
                    compareMode = false,
                    picked = false,
                    onClick = { onPick(view.summary.session.id) },
                )
            }
        }
    }
}
