package com.yudha.catatanbelanja.android.screen.dashboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.screen.dashboard.components.DashboardEmptyState
import com.yudha.catatanbelanja.android.screen.dashboard.components.DashboardRecentCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.DashboardStatsRow
import com.yudha.catatanbelanja.android.screen.dashboard.components.DashboardTopSpendingCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.DashboardTrendCard
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.presentation.DashboardEffect
import com.yudha.catatanbelanja.features.dashboard.presentation.DashboardViewModel
import org.koin.androidx.compose.koinViewModel

/** The Ringkasan tab — the prototype's `dashView()`. */
@Composable
fun DashboardScreen(
    onOpenSessionDetail: (String) -> Unit,
    onOpenSpendingReport: () -> Unit,
    onOpenSpendingRanking: () -> Unit,
    onOpenPriceTrend: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val demoAddedMessage = stringResource(R.string.common_demo_added)

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.effects.collect { effect ->
            when (effect) {
                DashboardEffect.DemoSeeded -> appUi.showToast(demoAddedMessage)
            }
        }
    }

    // Failures and the demo-seed spinner live in the state, not in the effect channel.
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

    val data = state.data
    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.dashboard_title),
                subtitle = if (state.hasAnySession) data.monthKey.monthKeyToLabel() else null,
                actions = {
                    AppIconButton(
                        onClick = onOpenSettings,
                        contentDescription = stringResource(R.string.common_cd_settings),
                        icon = Icons.Rounded.Settings,
                    )
                },
            )
        },
    ) {
        if (!state.hasAnySession) {
            // Don't flash "Belum ada data" while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) DashboardEmptyState(onSeedDemo = viewModel::seedDemo)
            return@AppScaffold
        }

        DashboardStatsRow(data = data)
        Spacer(Modifier.height(Spacing.x14))
        DashboardRecentCard(
            bars = data.recentBars,
            onOpenSessionDetail = onOpenSessionDetail,
            onSeeAll = onOpenSpendingReport,
        )
        Spacer(Modifier.height(Spacing.x14))
        DashboardTopSpendingCard(
            topItems = data.topItems,
            scope = state.scope,
            onSelectScope = viewModel::selectScope,
            onSeeAll = onOpenSpendingRanking,
        )

        if (state.trendNames.isEmpty()) return@AppScaffold

        Spacer(Modifier.height(Spacing.x14))
        DashboardTrendCard(
            trend = state.trend,
            names = state.trendNames,
            onSelectTrendItem = viewModel::selectTrendItem,
            onSeeAll = { onOpenPriceTrend(state.trend.name) },
        )
    }
}
