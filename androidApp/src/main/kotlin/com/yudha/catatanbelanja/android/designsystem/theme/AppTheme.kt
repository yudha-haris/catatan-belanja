package com.yudha.catatanbelanja.android.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

val LocalAppColors = staticCompositionLocalOf { purpleColors }
val LocalAppTypography = staticCompositionLocalOf { appTypographyFor(purpleColors) }
val LocalAppShapes = staticCompositionLocalOf { AppShapes() }

/** The only way UI code reaches colours, type and radii. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current
}

@Composable
fun AppTheme(flavor: ThemeFlavor, content: @Composable () -> Unit) {
    val colors = appColorsFor(flavor)
    val typography = remember(colors) { appTypographyFor(colors) }
    val shapes = remember { AppShapes() }

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppShapes provides shapes,
    ) {
        MaterialTheme(
            colorScheme = materialColorSchemeOf(colors),
            shapes = materialShapesOf(shapes),
            content = content,
        )
    }
}

/** Keeps Material 3 sheets, ripples and scrims on the active flavour. Light theme only. */
private fun materialColorSchemeOf(colors: AppColors): ColorScheme = lightColorScheme(
    primary = colors.primary,
    onPrimary = colors.paper,
    primaryContainer = colors.tint,
    onPrimaryContainer = colors.primaryDark,
    secondary = colors.primaryLight,
    onSecondary = colors.ink,
    secondaryContainer = colors.tint,
    onSecondaryContainer = colors.primaryDark,
    tertiary = colors.heroEnd,
    onTertiary = colors.paper,
    tertiaryContainer = colors.tint,
    onTertiaryContainer = colors.primaryDark,
    background = colors.background,
    onBackground = colors.ink,
    surface = colors.paper,
    onSurface = colors.ink,
    surfaceVariant = colors.tint,
    onSurfaceVariant = colors.inkSecondary,
    surfaceContainerLowest = colors.paper,
    surfaceContainerLow = colors.paper,
    surfaceContainer = colors.paper,
    surfaceContainerHigh = colors.paper,
    surfaceContainerHighest = colors.paper,
    surfaceTint = colors.primary,
    inverseSurface = colors.ink,
    inverseOnSurface = colors.paper,
    outline = colors.line,
    outlineVariant = colors.line,
    error = colors.coral,
    onError = colors.paper,
    errorContainer = colors.coralBg,
    onErrorContainer = colors.coral,
    scrim = colors.ink,
)

private fun materialShapesOf(shapes: AppShapes): Shapes = Shapes(
    extraSmall = RoundedCornerShape(shapes.radiusSmall),
    small = RoundedCornerShape(shapes.radiusSmall),
    medium = RoundedCornerShape(shapes.radiusItem),
    large = RoundedCornerShape(shapes.radius),
    extraLarge = RoundedCornerShape(shapes.radiusSheet),
)
