package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MILLIS = 1_800L

/**
 * The top pill toast. Slides down from above and reports back when its 1.8s is up.
 *
 * The countdown is keyed on [toastId], not on [message]: the same text shown twice in a row is
 * the same `String`, and a timer keyed on that would never restart.
 */
@Composable
fun AppToastHost(
    message: String?,
    toastId: Int,
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    // Keeps the last text on screen while the pill slides back out.
    val shown = remember { mutableStateOf("") }
    if (message != null) shown.value = message
    LaunchedEffect(toastId) {
        if (message == null) return@LaunchedEffect
        delay(TOAST_DURATION_MILLIS)
        onTimeout()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = 350,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.2f),
                ),
                initialOffsetY = { fullHeight -> -fullHeight - 80 },
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 250),
                targetOffsetY = { fullHeight -> -fullHeight - 80 },
            ),
        ) {
            // The app's only feedback channel, so screen readers have to be told it appeared.
            Text(
                text = shown.value,
                modifier = Modifier
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .background(colors.ink, RoundedCornerShape(AppTheme.shapes.pill))
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                style = AppTheme.typography.label,
                color = colors.paper,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
