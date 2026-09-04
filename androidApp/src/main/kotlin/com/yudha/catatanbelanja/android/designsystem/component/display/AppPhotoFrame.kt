package com.yudha.catatanbelanja.android.designsystem.component.display

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

/**
 * A photo read straight off the file system, decoded off the main thread.
 *
 * The file can be gone — cleared by the system, restored onto a phone that never had the picture,
 * or wiped by hand — so a missing image is a first-class state rather than an error: the frame
 * shows [missingLabel] and the trip carries on without it.
 */
@Composable
fun AppPhotoFrame(
    path: String,
    contentDescription: String,
    missingLabel: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusItem)
    val interactionSource = remember { MutableInteractionSource() }
    // Compose-local, and a pure UI concern: the decoded bitmap is what this frame draws with and
    // is of no interest to anything above it. Keyed on the path, so retaking the photo re-decodes.
    var image by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    // Decoding and having failed are different states. Without this the frame would accuse the
    // file of being gone for the frames it takes to read a large photo off disk.
    var isDecoded by remember(path) { mutableStateOf(false) }
    LaunchedEffect(path) {
        image = withContext(Dispatchers.IO) { decodeReceiptPhoto(path) }
        isDecoded = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.background)
            .then(
                when (onClick) {
                    null -> Modifier
                    else -> Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = image
        if (bitmap == null) {
            // Still reading: an empty frame of the right size, and no spinner. A photo that lands
            // in two frames does not need one, and one that flashes reads as a glitch.
            if (!isDecoded) return@Box

            Text(
                text = missingLabel,
                modifier = Modifier.padding(Spacing.x20),
                style = AppTheme.typography.muted,
                textAlign = TextAlign.Center,
            )
            return@Box
        }

        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxWidth(),
            contentScale = contentScale,
        )
    }
}

/**
 * Decodes at most [MAX_DISPLAY_PIXELS] on the long edge. The stored file is already scaled down,
 * so this is a guard rather than a resize — a picture restored from somewhere else could be any
 * size, and full-resolution bitmaps are how a list of receipts runs a phone out of memory.
 */
private fun decodeReceiptPhoto(path: String): ImageBitmap? {
    val file = File(path)
    if (!file.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    val longestEdge = max(bounds.outWidth, bounds.outHeight)
    if (longestEdge <= 0) return null

    var sample = 1
    while (longestEdge / sample > MAX_DISPLAY_PIXELS) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, options)?.asImageBitmap()
}

private const val MAX_DISPLAY_PIXELS = 1280
