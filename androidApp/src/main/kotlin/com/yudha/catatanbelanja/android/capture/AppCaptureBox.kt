package com.yudha.catatanbelanja.android.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Draws [content] through a graphics layer so [controller] can read it back as an image. The layer
 * is recorded on every frame and then drawn as usual, so what the user sees and what gets shared
 * are the same pixels by construction.
 */
@Composable
fun AppCaptureBox(
    controller: AppCaptureController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    DisposableEffect(controller, layer) {
        controller.layer = layer
        onDispose { controller.layer = null }
    }

    Box(
        modifier = modifier.drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
        },
    ) {
        content()
    }
}
