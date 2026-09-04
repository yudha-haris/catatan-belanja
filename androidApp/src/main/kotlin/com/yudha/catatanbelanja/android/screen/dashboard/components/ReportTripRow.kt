package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.android.format.toRupiahSigned
import com.yudha.catatanbelanja.features.dashboard.domain.model.TripSpending

/** One trip in the report. Tapping it opens the receipt, the way the history tab's rows do. */
@Composable
internal fun ReportTripRow(
    trip: TripSpending,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when {
        trip.hasName -> trip.name
        trip.hasStore -> trip.store
        else -> stringResource(R.string.report_trip_untitled)
    }
    val day = trip.endedAt.toDayLabel()
    // The store only earns its own slot once the title is not already showing it.
    val subtitle = when (trip.hasName && trip.hasStore) {
        true -> stringResource(R.string.report_trip_subtitle_store, day, trip.store, trip.itemCount)
        false -> stringResource(R.string.report_trip_subtitle, day, trip.itemCount)
    }
    val tone = when {
        trip.isUp -> AppBadgeTone.Up
        trip.isDown -> AppBadgeTone.Down
        else -> AppBadgeTone.Neutral
    }

    AppListRow(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        trailing = trip.total.toRupiah(),
        trailingSub = if (trip.hasDelta) trip.deltaAmount.toRupiahSigned() else null,
        trailingSubTone = tone,
        emoji = "🧾",
        onClick = onClick,
    )
}
