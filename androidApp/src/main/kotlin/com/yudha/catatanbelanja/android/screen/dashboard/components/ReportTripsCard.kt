package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.dashboard.domain.model.TripSpending

/**
 * Every trip in the window, newest first. Not an [androidx.compose.foundation.lazy.LazyColumn]:
 * the page already scrolls as one column, and a scroller inside a scroller is a trap.
 */
@Composable
internal fun ReportTripsCard(
    trips: List<TripSpending>,
    onOpenTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.report_trips_title),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.report_stat_trips_value, trips.size),
                style = AppTheme.typography.tiny,
            )
        }

        trips.forEach { trip ->
            ReportTripRow(trip = trip, onClick = { onOpenTrip(trip.sessionId) })
        }
    }
}
