package com.yudha.catatanbelanja.android.designsystem.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * Screen shell: a centred column capped at 440.dp with the standard screen padding, an optional
 * scrolling [header] and a floating [bottomBar] pinned above the navigation bar.
 *
 * The content area shrinks for the keyboard; [bottomBar] does not, so the pinned action bar hides
 * behind the keyboard rather than eating a third of what is left to type into.
 */
@Composable
fun AppScaffold(
    header: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    backgroundColor: Color = AppTheme.colors.background,
    contentPadding: PaddingValues = AppTheme.shapes.screenPadding,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = AppTheme.shapes.maxContentWidth)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                // The activity is edge-to-edge, so the window never resizes for the keyboard and
                // the manifest's `adjustResize` is inert. Without this the scroll viewport keeps
                // its full height, and a field that takes focus has nowhere to scroll into — it
                // just sits under the keyboard, which is what moving focus to "Harga" looked like.
                .imePadding()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(contentPadding),
        ) {
            if (header != null) header()
            content()
        }

        if (bottomBar == null) return@Box

        // Inside the shell the tab bar floats over us, and its measured height already clears the
        // navigation bar — so lift by that instead of applying the inset a second time.
        val shellInset = LocalShellBottomInset.current
        val liftAboveShellChrome = when (shellInset > 0.dp) {
            true -> Modifier.padding(bottom = shellInset)
            false -> Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = AppTheme.shapes.maxContentWidth)
                .fillMaxWidth()
                .then(liftAboveShellChrome)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            bottomBar()
        }
    }
}
