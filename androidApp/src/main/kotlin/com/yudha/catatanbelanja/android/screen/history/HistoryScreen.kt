package com.yudha.catatanbelanja.android.screen.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.pluralStringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.screen.history.components.HistoryCompareBar
import com.yudha.catatanbelanja.android.screen.history.components.HistoryEmptyContent
import com.yudha.catatanbelanja.android.screen.history.components.HistorySessionList
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.history.presentation.HistoryEffect
import com.yudha.catatanbelanja.features.history.presentation.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

/** The Riwayat tab — the prototype's `riwayatView()`. */
@Composable
fun HistoryScreen(
    onOpenDetail: (String) -> Unit,
    onOpenCompare: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenScanReceipt: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    val demoAddedMessage = stringResource(R.string.common_demo_added)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryEffect.OpenDetail -> onOpenDetail(effect.sessionId)
                is HistoryEffect.OpenCompare -> onOpenCompare(effect.aId, effect.bId)
                HistoryEffect.DemoSeeded -> appUi.showToast(demoAddedMessage)
            }
        }
    }

    // A session finished or deleted on a pushed route must be gone when the tab comes back.
    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

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

    val compareBar: (@Composable () -> Unit)? = when (state.compareMode && state.canRunCompare) {
        true -> ({ HistoryCompareBar(onRunCompare = viewModel::runCompare) })
        false -> null
    }

    // The list scrolls itself, so the scaffold hands it the full height and no padding of its own.
    AppScaffold(
        modifier = modifier,
        scrollable = false,
        contentPadding = PaddingValues(),
        bottomBar = compareBar,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.history_title),
                subtitle = when (state.hasAny) {
                    true -> pluralStringResource(
                        R.plurals.history_session_count,
                        state.sessionCount,
                        state.sessionCount,
                    )

                    false -> null
                },
                actions = {
                    if (state.hasAny) {
                        AppButton(
                            text = when (state.compareMode) {
                                true -> stringResource(R.string.history_compare_cancel)
                                false -> stringResource(R.string.history_compare_cta)
                            },
                            onClick = viewModel::toggleCompareMode,
                            variant = AppButtonVariant.Ghost,
                            emoji = COMPARE_EMOJI.takeIf { !state.compareMode },
                            fillWidth = false,
                        )
                    }
                    AppIconButton(
                        onClick = onOpenScanReceipt,
                        contentDescription = stringResource(R.string.scan_title),
                        emoji = SCAN_EMOJI,
                    )
                    AppIconButton(
                        onClick = onOpenSettings,
                        contentDescription = stringResource(R.string.common_cd_settings),
                        icon = Icons.Rounded.Settings,
                    )
                },
            )
        },
    ) {
        if (!state.hasAny) {
            // Don't flash "Belum ada riwayat" while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) {
                HistoryEmptyContent(
                    onScanReceipt = onOpenScanReceipt,
                    onSeedDemo = viewModel::seedDemo,
                    modifier = Modifier.padding(AppTheme.shapes.listPadding),
                )
            }
            return@AppScaffold
        }

        HistorySessionList(
            state = state,
            onSessionClicked = viewModel::onSessionClicked,
            onQuickCompare = viewModel::quickCompare,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

private const val COMPARE_EMOJI = "⇄"
private const val SCAN_EMOJI = "🧾"
