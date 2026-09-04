package com.yudha.catatanbelanja.android.screen.shopping

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppIconButton
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.shopping.components.StartEmptyState
import com.yudha.catatanbelanja.android.screen.shopping.components.StartHeroCard
import com.yudha.catatanbelanja.android.screen.shopping.components.StartListCard
import com.yudha.catatanbelanja.android.screen.shopping.components.StartRecentSessions
import com.yudha.catatanbelanja.android.screen.shopping.components.StartResumeCard
import com.yudha.catatanbelanja.android.screen.shopping.components.StartStatsRow
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.shopping.domain.model.Greeting
import com.yudha.catatanbelanja.features.shopping.presentation.StartEffect
import com.yudha.catatanbelanja.features.shopping.presentation.StartViewModel
import org.koin.androidx.compose.koinViewModel

/** The Belanja tab — the prototype's `startView()`. */
@Composable
fun StartScreen(
    onOpenLiveSession: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StartViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current
    var store by remember { mutableStateOf("") }

    val sessionRunningMessage = stringResource(R.string.home_toast_session_running)
    val demoAddedMessage = stringResource(R.string.common_demo_added)

    // The tab is returned to right after a session is finished or cancelled, so it reloads on resume.
    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StartEffect.SessionStarted -> {
                    store = ""
                    onOpenLiveSession()
                }

                StartEffect.ActiveSessionExists -> {
                    appUi.showToast(sessionRunningMessage)
                    onOpenLiveSession()
                }

                StartEffect.DemoSeeded -> appUi.showToast(demoAddedMessage)
                is StartEffect.ShowError -> appUi.showError(effect.failure)
            }
        }
    }

    val greeting = when (state.greeting) {
        Greeting.MORNING -> stringResource(R.string.home_greeting_morning)
        Greeting.NOON -> stringResource(R.string.home_greeting_noon)
        Greeting.AFTERNOON -> stringResource(R.string.home_greeting_afternoon)
        Greeting.EVENING -> stringResource(R.string.home_greeting_evening)
    }

    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.home_title),
                subtitle = "$greeting 👋",
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
        val active = state.activeSession
        if (active != null) {
            StartResumeCard(summary = active, onClick = onOpenLiveSession)
            Spacer(Modifier.height(Spacing.x16))
        }

        // Plan, then go: the list sits above the "Mulai belanja" hero because that is the
        // order the trip actually happens in.
        StartListCard(
            hasList = state.hasList,
            remainingCount = state.listRemainingCount,
            previewNames = state.listPreviewNames,
            extraCount = state.listExtraCount,
            onOpenList = onOpenList,
        )
        Spacer(Modifier.height(Spacing.x16))

        StartHeroCard(
            hasActiveSession = active != null,
            store = store,
            onStoreChange = { store = it },
            storeSuggestions = state.storeSuggestions,
            onStartSession = { viewModel.startSession(store) },
        )

        if (!state.hasAnySession) {
            // Don't flash the first-run copy while the very first load is still in flight.
            val loaded = state.loadState is UiState.Success
            if (loaded) StartEmptyState(onSeedDemo = viewModel::seedDemo)
            return@AppScaffold
        }

        Spacer(Modifier.height(Spacing.x16))
        StartStatsRow(
            monthTotal = state.monthTotal,
            monthCount = state.monthCount,
            monthAverage = state.monthAverage,
        )
        AppSectionHeader(
            title = stringResource(R.string.home_recent_title),
            trailing = {
                AppChip(
                    text = stringResource(R.string.home_recent_see_all),
                    onClick = onOpenHistory,
                    variant = AppChipVariant.Plain,
                )
            },
        )
        StartRecentSessions(recent = state.recent, onOpenSessionDetail = onOpenSessionDetail)
    }
}
