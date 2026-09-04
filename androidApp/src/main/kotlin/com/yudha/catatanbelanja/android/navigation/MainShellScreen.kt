package com.yudha.catatanbelanja.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.android.designsystem.component.layout.LocalShellBottomInset
import com.yudha.catatanbelanja.android.screen.dashboard.DashboardScreen
import com.yudha.catatanbelanja.android.screen.history.HistoryScreen
import com.yudha.catatanbelanja.android.screen.shopping.StartScreen
import com.yudha.catatanbelanja.android.screen.stock.StockScreen
import com.yudha.catatanbelanja.features.dashboard.presentation.DashboardViewModel
import com.yudha.catatanbelanja.features.history.presentation.HistoryViewModel
import com.yudha.catatanbelanja.features.shopping.presentation.StartViewModel
import com.yudha.catatanbelanja.features.stock.presentation.StockViewModel
import org.koin.androidx.compose.koinViewModel

internal const val TAB_SHOPPING = 0
internal const val TAB_HISTORY = 1
internal const val TAB_STOCK = 2
internal const val TAB_DASHBOARD = 3

/**
 * The four tabs and the floating pill bar. All four stay composed at once, so switching tabs
 * keeps every scroll position, open sheet and text buffer exactly where the user left it.
 */
@Composable
fun MainShellScreen(
    onOpenLiveSession: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    onOpenCompare: (String, String) -> Unit,
    onOpenSpendingReport: () -> Unit,
    onOpenSpendingRanking: () -> Unit,
    onOpenPriceTrend: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_SHOPPING) }

    // The tab bar floats over the tabs, so a screen's pinned action bar has to be lifted clear of
    // it. Measured rather than hardcoded so it survives font scaling and taller navigation bars.
    val density = LocalDensity.current
    var tabBarHeight by remember { mutableStateOf(0.dp) }

    // Hoisted so the shell can reload the tab the user lands on. Same store owner as the screens
    // below, so `koinViewModel()` there hands back these very instances.
    val startViewModel: StartViewModel = koinViewModel()
    val historyViewModel: HistoryViewModel = koinViewModel()
    val stockViewModel: StockViewModel = koinViewModel()
    val dashboardViewModel: DashboardViewModel = koinViewModel()

    // The tab badge is the only place the app now tells you something needs buying, so it reads
    // the stock tab's own state rather than a second count computed somewhere else.
    val stockState by stockViewModel.state.collectAsStateWithLifecycle()

    // A tab that stayed composed will not reload itself, but a finished session, a demo seed, an
    // import or a wipe has to be there when the user comes back to it. Each `load()` guards
    // against a second run, so the tab's own first load is not duplicated.
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            TAB_SHOPPING -> startViewModel.load()
            TAB_HISTORY -> historyViewModel.load()
            TAB_STOCK -> stockViewModel.load()
            TAB_DASHBOARD -> dashboardViewModel.load()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalShellBottomInset provides tabBarHeight) {
            StartScreen(
                onOpenLiveSession = onOpenLiveSession,
                onOpenSessionDetail = onOpenSessionDetail,
                onOpenHistory = { selectedTab = TAB_HISTORY },
                onOpenSettings = onOpenSettings,
                onOpenList = onOpenList,
                modifier = Modifier.shownWhen(selectedTab == TAB_SHOPPING),
                viewModel = startViewModel,
            )

            HistoryScreen(
                onOpenDetail = onOpenSessionDetail,
                onOpenCompare = onOpenCompare,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.shownWhen(selectedTab == TAB_HISTORY),
                viewModel = historyViewModel,
            )

            StockScreen(
                onOpenSettings = onOpenSettings,
                modifier = Modifier.shownWhen(selectedTab == TAB_STOCK),
                viewModel = stockViewModel,
            )

            DashboardScreen(
                onOpenSessionDetail = onOpenSessionDetail,
                onOpenSpendingReport = onOpenSpendingReport,
                onOpenSpendingRanking = onOpenSpendingRanking,
                onOpenPriceTrend = onOpenPriceTrend,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.shownWhen(selectedTab == TAB_DASHBOARD),
                viewModel = dashboardViewModel,
            )
        }

        ShellTabBar(
            selectedIndex = selectedTab,
            onSelect = { index -> selectedTab = index },
            stockBadgeCount = stockState.lowCount,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { size ->
                    tabBarHeight = with(density) { size.height.toDp() }
                },
        )
    }
}

/**
 * Keeps a hidden tab composed but out of the layout: it is still measured, so its lists keep
 * their state, and never placed, so it neither draws nor takes a touch.
 */
private fun Modifier.shownWhen(shown: Boolean): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val width = if (shown) placeable.width else 0
    val height = if (shown) placeable.height else 0

    layout(width, height) {
        if (!shown) return@layout
        placeable.place(0, 0)
    }
}
