package com.yudha.catatanbelanja.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yudha.catatanbelanja.android.screen.dashboard.PriceTrendScreen
import com.yudha.catatanbelanja.android.screen.dashboard.SpendingRankingScreen
import com.yudha.catatanbelanja.android.screen.dashboard.SpendingReportScreen
import com.yudha.catatanbelanja.android.screen.history.CompareScreen
import com.yudha.catatanbelanja.android.screen.history.SessionDetailScreen
import com.yudha.catatanbelanja.android.screen.list.ShoppingListScreen
import com.yudha.catatanbelanja.android.screen.preset.PresetBrandsScreen
import com.yudha.catatanbelanja.android.screen.preset.PresetCategoriesScreen
import com.yudha.catatanbelanja.android.screen.preset.PresetHubScreen
import com.yudha.catatanbelanja.android.screen.preset.PresetItemsScreen
import com.yudha.catatanbelanja.android.screen.preset.PresetLanguageScreen
import com.yudha.catatanbelanja.android.screen.receipt.ScanReceiptScreen
import com.yudha.catatanbelanja.android.screen.settings.SettingsScreen
import com.yudha.catatanbelanja.android.screen.shopping.LiveSessionScreen

/**
 * The whole graph. [Shell][AppDestination.Shell] holds the four tabs; everything else is pushed
 * full-screen over it, so no pushed route ever draws the tab bar.
 *
 * Tabs reload themselves: a pushed route replaces the shell in the composition, so returning to
 * it re-runs each tab's own load effect. The theme needs no such hook — it streams from the
 * database, so a colour picked in Settings applies before the screen even closes.
 */
@Composable
fun AppNavHost(
    openLiveSessionOnStart: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // The prototype's boot block: `if (S.active) V.view = 'live'`. Saved, so a rotation does not
    // push a second copy of the live screen onto the stack.
    var didBoot by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openLiveSessionOnStart) {
        if (didBoot) return@LaunchedEffect
        didBoot = true
        if (!openLiveSessionOnStart) return@LaunchedEffect

        navController.navigate(AppDestination.LiveSession().route)
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Shell.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Pattern.SHELL) {
            MainShellScreen(
                onOpenLiveSession = {
                    navController.navigate(AppDestination.LiveSession().route)
                },
                onOpenSessionDetail = { sessionId ->
                    navController.navigate(AppDestination.SessionDetail(sessionId).route)
                },
                onOpenCompare = { aId, bId ->
                    navController.navigate(AppDestination.Compare(aId, bId).route)
                },
                onOpenSpendingReport = {
                    navController.navigate(AppDestination.SpendingReport.route)
                },
                onOpenSpendingRanking = {
                    navController.navigate(AppDestination.SpendingRanking.route)
                },
                onOpenPriceTrend = { name ->
                    navController.navigate(AppDestination.PriceTrend(name).route)
                },
                onOpenSettings = {
                    navController.navigate(AppDestination.Settings.route)
                },
                onOpenList = {
                    navController.navigate(AppDestination.ShoppingList.route)
                },
                onOpenScanReceipt = {
                    navController.navigate(AppDestination.ScanReceipt.route)
                },
            )
        }

        composable(
            route = AppDestination.Pattern.LIVE_SESSION,
            arguments = listOf(
                navArgument(AppDestination.Arg.REPEAT_FROM) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            LiveSessionScreen(
                onBack = { navController.popBackStack() },
                onSessionFinished = { sessionId ->
                    // The finished session must not stay behind the receipt it just became.
                    navController.popBackStack(AppDestination.Pattern.SHELL, inclusive = false)
                    navController.navigate(AppDestination.SessionDetail(sessionId).route)
                },
                repeatFromSessionId = entry.arguments?.getString(AppDestination.Arg.REPEAT_FROM),
            )
        }

        composable(
            route = AppDestination.Pattern.SESSION_DETAIL,
            arguments = listOf(navArgument(AppDestination.Arg.SESSION_ID) { type = NavType.StringType }),
        ) { entry ->
            val sessionId = entry.arguments?.getString(AppDestination.Arg.SESSION_ID).orEmpty()
            SessionDetailScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onOpenCompare = { aId, bId ->
                    navController.navigate(AppDestination.Compare(aId, bId).route)
                },
                // The screen hands over the repeated names, but the live view model re-reads them
                // from the session itself, so the route only has to carry the id.
                onOpenLiveSession = {
                    navController.popBackStack()
                    navController.navigate(AppDestination.LiveSession(sessionId).route)
                },
            )
        }

        composable(
            route = AppDestination.Pattern.COMPARE,
            arguments = listOf(
                navArgument(AppDestination.Arg.A_ID) { type = NavType.StringType },
                navArgument(AppDestination.Arg.B_ID) { type = NavType.StringType },
            ),
        ) { entry ->
            CompareScreen(
                aId = entry.arguments?.getString(AppDestination.Arg.A_ID).orEmpty(),
                bId = entry.arguments?.getString(AppDestination.Arg.B_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppDestination.Pattern.SPENDING_REPORT) {
            SpendingReportScreen(
                onBack = { navController.popBackStack() },
                onOpenSessionDetail = { sessionId ->
                    navController.navigate(AppDestination.SessionDetail(sessionId).route)
                },
            )
        }

        composable(AppDestination.Pattern.SPENDING_RANKING) {
            SpendingRankingScreen(
                onBack = { navController.popBackStack() },
                onOpenPriceTrend = { name ->
                    navController.navigate(AppDestination.PriceTrend(name).route)
                },
            )
        }

        composable(
            route = AppDestination.Pattern.PRICE_TREND,
            arguments = listOf(
                navArgument(AppDestination.Arg.TREND_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            PriceTrendScreen(
                initialName = entry.arguments?.getString(AppDestination.Arg.TREND_NAME),
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppDestination.Pattern.SHOPPING_LIST) {
            ShoppingListScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.Pattern.SCAN_RECEIPT) {
            ScanReceiptScreen(
                onBack = { navController.popBackStack() },
                // The scan screen must not stay behind the trip it just created: backing out of
                // that receipt belongs on the history tab, not on a spent draft.
                onOpenSessionDetail = { sessionId ->
                    navController.popBackStack()
                    navController.navigate(AppDestination.SessionDetail(sessionId).route)
                },
            )
        }

        composable(AppDestination.Pattern.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPreset = { navController.navigate(AppDestination.Preset.route) },
            )
        }

        composable(AppDestination.Pattern.PRESET) {
            PresetHubScreen(
                onBack = { navController.popBackStack() },
                onOpenItems = { navController.navigate(AppDestination.PresetItems.route) },
                onOpenCategories = {
                    navController.navigate(AppDestination.PresetCategories.route)
                },
                onOpenBrands = { navController.navigate(AppDestination.PresetBrands.route) },
                onOpenLanguage = { navController.navigate(AppDestination.PresetLanguage.route) },
            )
        }

        composable(AppDestination.Pattern.PRESET_ITEMS) {
            PresetItemsScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.Pattern.PRESET_CATEGORIES) {
            PresetCategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.Pattern.PRESET_BRANDS) {
            PresetBrandsScreen(onBack = { navController.popBackStack() })
        }

        composable(AppDestination.Pattern.PRESET_LANGUAGE) {
            PresetLanguageScreen(onBack = { navController.popBackStack() })
        }
    }
}
