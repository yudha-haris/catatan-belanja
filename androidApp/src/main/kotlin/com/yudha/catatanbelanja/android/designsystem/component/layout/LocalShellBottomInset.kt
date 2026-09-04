package com.yudha.catatanbelanja.android.designsystem.component.layout

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * How much chrome the app shell floats underneath a screen — the tab bar plus its own bottom
 * inset. [AppScaffold] lifts its bottomBar by this much so a pinned action bar clears the tabs
 * instead of hiding behind them. This is the prototype's `.finish { bottom: 84px }` versus its
 * `body.no-nav .finish { bottom: 14px }`.
 *
 * Zero by default, which is correct for pushed routes: they have no tab bar under them.
 */
val LocalShellBottomInset = compositionLocalOf { 0.dp }
