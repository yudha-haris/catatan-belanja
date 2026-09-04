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
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.dashboard.components.TrendBasisCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.TrendChartCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.TrendHistoryCard
import com.yudha.catatanbelanja.android.screen.dashboard.components.TrendItemPickerSheet
import com.yudha.catatanbelanja.android.screen.dashboard.components.TrendQtySheet
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.presentation.PriceTrendEffect
import com.yudha.catatanbelanja.features.dashboard.presentation.PriceTrendViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * "Tren harga" — one item's price over time, and the page that makes it honest.
 *
 * The chart is deliberately dumb by default: it plots what the item cost each trip, which is the
 * number the user recognises. That reading breaks the moment the amount bought changes, so the
 * basis card lets them switch the item to a unit price, and the purchase list below lets them fill
 * in the quantities the receipts never captured. Both are saved per item, the instant they are set.
 */
@Composable
fun PriceTrendScreen(
    initialName: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PriceTrendViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val invalidQtyMessage = stringResource(R.string.trend_msg_invalid_qty)
    val savedMessage = stringResource(R.string.trend_msg_saved)
    val clearedMessage = stringResource(R.string.trend_msg_cleared)

    LaunchedEffect(Unit) {
        viewModel.load(initialName)
        viewModel.effects.collect { effect ->
            when (effect) {
                is PriceTrendEffect.ShowMessage -> when (effect.kind) {
                    PriceTrendEffect.Kind.INVALID_QTY -> appUi.showToast(invalidQtyMessage)
                    PriceTrendEffect.Kind.ADJUSTMENT_SAVED -> appUi.showToast(savedMessage)
                    PriceTrendEffect.Kind.ADJUSTMENT_CLEARED -> appUi.showToast(clearedMessage)
                }
            }
        }
    }

    LaunchedEffect(state.loadState) {
        val load = state.loadState
        if (load !is UiState.Error) return@LaunchedEffect
        appUi.showError(load.failure)
    }

    // No loading dialog for actions here: every one of them is a toggle or a two-field sheet over a
    // local database, and a spinner that flashes for one frame reads as a glitch.
    LaunchedEffect(state.actionState) {
        val action = state.actionState
        if (action !is UiState.Error) return@LaunchedEffect
        appUi.showError(action.failure)
    }

    val data = state.data
    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.trend_title),
                subtitle = data.name.takeIf { it.isNotBlank() },
                onBack = onBack,
            )
        },
    ) {
        if (!state.hasAnyCandidate) {
            // Don't flash the empty state while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) {
                AppEmptyState(
                    emoji = "📈",
                    title = stringResource(R.string.trend_empty_title),
                    message = stringResource(R.string.trend_empty_message),
                )
            }
            return@AppScaffold
        }

        AppListRow(
            title = data.name,
            subtitle = stringResource(R.string.trend_change_item),
            emoji = data.emoji,
            onClick = viewModel::openPicker,
        )
        Spacer(Modifier.height(Spacing.x14))
        TrendBasisCard(
            data = data,
            onSelectBasis = viewModel::selectBasis,
            onSelectBaseUnit = viewModel::selectBaseUnit,
        )
        Spacer(Modifier.height(Spacing.x14))

        when (data.hasTrend) {
            true -> TrendChartCard(data = data)
            false -> AppEmptyState(
                emoji = "🧮",
                title = stringResource(R.string.trend_thin_title),
                message = stringResource(R.string.trend_thin_message),
            )
        }
        Spacer(Modifier.height(Spacing.x14))
        TrendHistoryCard(
            purchases = data.purchases,
            basis = data.basis,
            baseUnit = data.baseUnit,
            onOpenPurchase = viewModel::openQtySheet,
        )
    }

    if (state.isPickerOpen) {
        TrendItemPickerSheet(
            candidates = state.visibleCandidates,
            query = state.query,
            onQueryChanged = viewModel::onQueryChanged,
            onSelect = viewModel::selectName,
            onDismiss = viewModel::dismissPicker,
        )
    }

    val editing = state.editing ?: return
    TrendQtySheet(
        purchase = editing,
        unitOptions = state.editingUnitOptions,
        enabled = state.actionState !is UiState.Loading,
        onSave = viewModel::saveQtyOverride,
        onClear = viewModel::clearQtyOverride,
        onDismiss = viewModel::dismissQtySheet,
    )
}
