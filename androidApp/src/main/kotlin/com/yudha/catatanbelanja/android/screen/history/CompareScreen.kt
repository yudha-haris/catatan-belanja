package com.yudha.catatanbelanja.android.screen.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.feedback.LocalAppUi
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScaffold
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppScreenHeader
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.screen.history.components.CompareBothSection
import com.yudha.catatanbelanja.android.screen.history.components.CompareHeaderCards
import com.yudha.catatanbelanja.android.screen.history.components.CompareOnlySection
import com.yudha.catatanbelanja.android.screen.history.components.CompareTotalCard
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.history.presentation.CompareViewModel
import org.koin.androidx.compose.koinViewModel

/** Side-by-side A/B diff of two sessions — the prototype's `compareView()`. */
@Composable
fun CompareScreen(
    aId: String,
    bId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appUi = LocalAppUi.current

    LaunchedEffect(aId, bId) {
        viewModel.load(aId, bId)
    }

    LaunchedEffect(state.loadState) {
        val load = state.loadState
        if (load !is UiState.Error) return@LaunchedEffect
        appUi.showError(load.failure)
    }

    val result = state.result
    val sessionA = state.sessionA
    val sessionB = state.sessionB

    AppScaffold(
        modifier = modifier,
        header = {
            AppScreenHeader(
                title = stringResource(R.string.compare_title),
                subtitle = pluralStringResource(
                    R.plurals.compare_subtitle,
                    result.sameCount,
                    result.sameCount,
                    result.differentCount,
                ),
                onBack = onBack,
            )
        },
    ) {
        if (sessionA == null || sessionB == null) return@AppScaffold

        CompareHeaderCards(sessionA = sessionA, sessionB = sessionB)
        Spacer(Modifier.height(Spacing.x10))
        CompareTotalCard(result = result)
        CompareBothSection(rows = result.inBoth)
        CompareOnlySection(
            title = stringResource(R.string.compare_only_a),
            total = result.onlyInATotal,
            rows = result.onlyInA,
        )
        CompareOnlySection(
            title = stringResource(R.string.compare_only_b),
            total = result.onlyInBTotal,
            rows = result.onlyInB,
        )
    }
}
