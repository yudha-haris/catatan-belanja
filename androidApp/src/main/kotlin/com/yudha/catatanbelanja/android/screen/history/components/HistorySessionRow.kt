package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.features.history.domain.model.HistorySessionRowView

private const val RECEIPT_EMOJI = "🧾"

/** One finished session in the history list; in compare mode the badge becomes a pick check. */
@Composable
internal fun HistorySessionRow(
    view: HistorySessionRowView,
    compareMode: Boolean,
    picked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = view.summary
    val session = summary.session
    val dayLabel = (session.endedAt ?: session.startedAt).toDayLabel()
    val subtitle = when (view.showStore) {
        true -> pluralStringResource(
            R.plurals.history_row_subtitle_with_store,
            summary.itemCount,
            dayLabel,
            summary.itemCount,
            session.store,
        )
        false -> pluralStringResource(
            R.plurals.history_row_subtitle,
            summary.itemCount,
            dayLabel,
            summary.itemCount,
        )
    }
    val leading: (@Composable () -> Unit)? = when (compareMode) {
        true -> ({ HistoryPickCheck(picked = picked) })
        false -> null
    }

    AppListRow(
        title = session.name,
        modifier = modifier,
        subtitle = subtitle,
        trailing = summary.total.toRupiah(),
        emoji = RECEIPT_EMOJI,
        leading = leading,
        selected = picked,
        onClick = onClick,
    )
}
