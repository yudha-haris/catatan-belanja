package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.domain.model.SessionSummary

/** The `.quick` list: the last three finished sessions. Three rows, so no lazy list. */
@Composable
internal fun StartRecentSessions(
    recent: List<SessionSummary>,
    onOpenSessionDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x6),
    ) {
        recent.forEach { summary ->
            val session = summary.session
            AppListRow(
                title = session.name,
                subtitle = pluralStringResource(
                    R.plurals.home_recent_subtitle,
                    summary.itemCount,
                    (session.endedAt ?: session.startedAt).toDayLabel(),
                    summary.itemCount,
                ),
                trailing = summary.total.toRupiah(),
                emoji = "🧾",
                dense = true,
                onClick = { onOpenSessionDetail(session.id) },
            )
        }
    }
}
