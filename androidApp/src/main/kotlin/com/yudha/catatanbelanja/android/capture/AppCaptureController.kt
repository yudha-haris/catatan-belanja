package com.yudha.catatanbelanja.android.capture

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * The handle an [AppCaptureBox] hands its caller: "draw what is inside that box into a PNG".
 *
 * The layer records the node's own drawing at the node's own size, so content taller than the
 * screen is captured whole — being scrolled out of view or clipped by an ancestor does not lose
 * any of it. It does have to be composed and drawn at least once first, which is why the box is
 * something the user can see rather than something rendered off to one side.
 */
@Stable
class AppCaptureController {

    internal var layer: GraphicsLayer? = null

    /** PNG bytes, or null when nothing has been drawn yet. */
    suspend fun capturePng(): ByteArray? {
        val image = layer?.toImageBitmap() ?: return null
        return withContext(Dispatchers.Default) {
            val bitmap = image.asAndroidBitmap()
            // A hardware bitmap has no pixels this process can read, and everything from API 29 up
            // hands one back. Copying to ARGB_8888 is the supported way to get them.
            val readable = when (bitmap.config) {
                Bitmap.Config.HARDWARE -> bitmap.copy(Bitmap.Config.ARGB_8888, false)
                else -> bitmap
            } ?: return@withContext null

            ByteArrayOutputStream().use { stream ->
                readable.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
                stream.toByteArray()
            }
        }
    }

    private companion object {
        /** Ignored for PNG, which is lossless, but the parameter is not optional. */
        const val PNG_QUALITY = 100
    }
}
