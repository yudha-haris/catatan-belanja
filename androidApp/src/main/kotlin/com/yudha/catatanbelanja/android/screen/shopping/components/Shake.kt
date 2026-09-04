package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** The prototype's 400ms `.shake` keyframes: four decreasing swings back to centre. */
private val SWINGS = listOf(-8f, 8f, -6f, 6f, 0f)
private const val SWING_MILLIS = 80

/** Shakes the field whenever [trigger] changes. A trigger of 0 is the untouched state. */
@Composable
internal fun Modifier.shake(trigger: Int): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect

        SWINGS.forEach { offset.animateTo(it, tween(durationMillis = SWING_MILLIS)) }
    }
    return this.graphicsLayer { translationX = offset.value }
}
