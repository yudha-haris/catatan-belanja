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
import com.yudha.catatanbelanja.android.screen.dashboard.components.RankingListCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.RankingModeChips
import com.yudha.catatanbelanja.android.screen.dashboard.components.RankingShareCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.ReportRangeChips
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.presentation.SpendingRankingViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * "Pengeluaran terbesar" — the page behind the summary tab's top five.
 *
 * The tab lists five items; this adds the two things a list of five cannot show: the whole tail,
 * and how much of the money the head of it accounts for.
 */
@Composable
fun SpendingRankingScreen(
    onBack: () -> Unit,
    onOpenPriceTrend: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpendingRankingViewModel = koinViewModel(),
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
                title = stringResource(R.string.ranking_title),
                subtitle = when (data.hasEntries) {
                    true -> stringResource(
                        R.string.ranking_subtitle,
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
        Spacer(Modifier.height(Spacing.x10))
        RankingModeChips(selected = state.mode, onSelect = viewModel::selectMode)
        Spacer(Modifier.height(Spacing.x14))

        if (!data.hasEntries) {
            // Don't flash the empty state while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) {
                AppEmptyState(
                    emoji = "🏆",
                    title = stringResource(R.string.ranking_empty_title),
                    message = stringResource(R.string.ranking_empty_message),
                )
            }
            return@AppScaffold
        }

        RankingShareCard(
            slices = data.slices,
            leaderLabel = data.leaderLabel,
            leaderPercent = data.leaderPercent,
        )
        Spacer(Modifier.height(Spacing.x14))
        RankingListCard(
            entries = data.entries,
            mode = state.mode,
            onOpenTrend = onOpenPriceTrend,
        )
    }
}
