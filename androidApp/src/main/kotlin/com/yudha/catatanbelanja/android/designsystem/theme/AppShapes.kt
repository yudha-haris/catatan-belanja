package com.yudha.catatanbelanja.android.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One elevated-surface recipe. The colour is always `ink`; only the elevation and the two
 * alphas differ, so the shadow follows the flavour without living in [AppColors].
 */
@Immutable
data class AppShadow(
    val elevation: Dp,
    val ambientAlpha: Float,
    val spotAlpha: Float,
)

@Immutable
data class AppShapes(
    val radius: Dp = 22.dp,
    val radiusSmall: Dp = 14.dp,
    val radiusItem: Dp = 18.dp,
    val radiusSheet: Dp = 28.dp,
    val pill: Dp = 999.dp,
    val maxContentWidth: Dp = 440.dp,
    val screenPadding: PaddingValues = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 150.dp),
    /** `0 10px 30px -12px rgba(ink,.18)` from the prototype. */
    val cardShadow: AppShadow = AppShadow(elevation = 10.dp, ambientAlpha = 0.18f, spotAlpha = 0.18f),
    /** `0 12px 40px -10px rgba(ink,.30)` — the floating tab bar sits higher off the page. */
    val tabBarShadow: AppShadow = AppShadow(elevation = 12.dp, ambientAlpha = 0.30f, spotAlpha = 0.30f),
)

/** Vertical / horizontal gaps. Used as `Spacer(Modifier.height(Spacing.x12))`. */
object Spacing {
    val x4: Dp = 4.dp
    val x6: Dp = 6.dp
    val x8: Dp = 8.dp
    val x10: Dp = 10.dp
    val x12: Dp = 12.dp
    val x14: Dp = 14.dp
    val x16: Dp = 16.dp
    val x18: Dp = 18.dp
    val x20: Dp = 20.dp
    val x22: Dp = 22.dp
    val x24: Dp = 24.dp
    val x28: Dp = 28.dp
    val x32: Dp = 32.dp
}

/** Applies [spec] tinted with the flavour's `ink`, so no component needs a literal colour. */
@Composable
fun Modifier.appShadow(shape: Shape, spec: AppShadow = AppTheme.shapes.cardShadow): Modifier {
    val ink = AppTheme.colors.ink
    return shadow(
        elevation = spec.elevation,
        shape = shape,
        clip = false,
        ambientColor = ink.copy(alpha = spec.ambientAlpha),
        spotColor = ink.copy(alpha = spec.spotAlpha),
    )
}
