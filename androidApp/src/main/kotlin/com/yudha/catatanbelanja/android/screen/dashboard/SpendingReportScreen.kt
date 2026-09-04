package com.yudha.catatanbelanja.android.screen.dashboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppEmptyState
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.android.screen.dashboard.components.ReportMonthsCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.ReportRangeChips
import com.yudha.catatanbelanja.android.screen.dashboard.components.ReportStatsGrid
import com.yudha.catatanbelanja.android.screen.dashboard.components.ReportTripsCard
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.presentation.SpendingReportViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * "Laporan belanja" — the page behind the summary tab's eight bars.
 *
 * The tab answers "what did the last few trips cost"; this answers "what is a month of this
 * actually costing me", which is the question the eight bars keep raising and cannot settle.
 */
@Composable
fun SpendingReportScreen(
    onBack: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpendingReportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.loadState) {
        val load = state.loadState
        if (load !is UiState.Error) return@LaunchedEffect
        appUi.showError(load.failure)
    }

    val data = state.data
    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.report_title),
                subtitle = when (data.hasAnyTrip) {
                    true -> stringResource(
                        R.string.report_subtitle,
                        data.tripCount,
                        data.total.toRupiahShort(),
                    )
                    false -> null
                },
                onBack = onBack,
            )
        },
    ) {
        ReportRangeChips(
            options = state.rangeOptions,
            selected = state.range,
            onSelect = viewModel::selectRange,
        )
        Spacer(Modifier.height(Spacing.x14))

        if (!data.hasAnyTrip) {
            // Don't flash the empty state while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) {
                AppEmptyState(
                    emoji = "🗓️",
                    title = stringResource(R.string.report_empty_title),
                    message = stringResource(R.string.report_empty_message),
                )
            }
            return@AppScaffold
        }

        ReportStatsGrid(data = data, onOpenBiggestTrip = onOpenSessionDetail)
        Spacer(Modifier.height(Spacing.x14))
        ReportMonthsCard(bars = data.monthBars, months = data.months)
        Spacer(Modifier.height(Spacing.x14))
        ReportTripsCard(trips = data.trips, onOpenTrip = onOpenSessionDetail)
    }
}
