package com.yudha.catatanbelanja.android.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The long edge every receipt photo is cut down to before it leaves this file. */
private const val MAX_EDGE = 1600
private const val JPEG_QUALITY = 82
private const val QUARTER_TURN = 90
private const val HALF_TURN = 180
private const val THREE_QUARTER_TURN = 270

/**
 * Reads the picture at [uri] into the modest JPEG the rest of the app deals in — capped at
 * [MAX_EDGE] on its long edge, EXIF rotation already applied. Null when the image cannot be
 * opened or decoded at all: a deleted file, a share that expired, something that is not an image.
 *
 * Downsizing here rather than later is the point. A camera hands back an eight-megabyte, 4000-pixel
 * photograph of mostly white paper; storing that per trip would dwarf the database, and sending it
 * to the receipt scanner would cost several times more per scan without reading any better.
 *
 * `ImageDecoder` from API 28 applies the orientation tag itself and scales while decoding, so the
 * full-size bitmap never exists. Below that — API 26 and 27 — `BitmapFactory` needs both done by
 * hand, and a receipt photographed in portrait is very often stored sideways with a rotation tag.
 */
internal suspend fun readReceiptPhoto(context: Context, uri: Uri): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeUpright(context, uri) ?: return@runCatching null

            ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.toByteArray()
            }
        }.getOrNull()
    }

/**
 * A plain `if` rather than a `when`, because lint's API check only reads the `if` form — and an
 * unguarded `ImageDecoder` call would crash on the API 26 and 27 devices this app still supports.
 */
private fun decodeUpright(context: Context, uri: Uri): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return decodeWithImageDecoder(context, uri)
    return decodeWithBitmapFactory(context, uri)
}

@RequiresApi(Build.VERSION_CODES.P)
private fun decodeWithImageDecoder(context: Context, uri: Uri): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        // A hardware bitmap has no readable pixels, and `compress` needs to read them.
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val longestEdge = maxOf(info.size.width, info.size.height)
        if (longestEdge > MAX_EDGE) {
            val ratio = MAX_EDGE.toFloat() / longestEdge
            decoder.setTargetSize(
                (info.size.width * ratio).toInt().coerceAtLeast(1),
                (info.size.height * ratio).toInt().coerceAtLeast(1),
            )
        }
    }
}

private fun decodeWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.open(uri) { BitmapFactory.decodeStream(it, null, bounds) }

    val longestEdge = maxOf(bounds.outWidth, bounds.outHeight)
    if (longestEdge <= 0) return null

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSizeFor(longestEdge) }
    val decoded = context.open(uri) { BitmapFactory.decodeStream(it, null, options) } ?: return null
    val rotation = context.open(uri) { it.readRotation() } ?: 0
    return decoded.rotated(rotation).cappedTo(MAX_EDGE)
}

private fun <T> Context.open(uri: Uri, read: (InputStream) -> T): T? =
    contentResolver.openInputStream(uri)?.use(read)

private fun InputStream.readRotation(): Int {
    val orientation = ExifInterface(this)
        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> QUARTER_TURN
        ExifInterface.ORIENTATION_ROTATE_180 -> HALF_TURN
        ExifInterface.ORIENTATION_ROTATE_270 -> THREE_QUARTER_TURN
        else -> 0
    }
}

/**
 * The power-of-two shrink `BitmapFactory` applies while decoding, so a large photograph is never
 * held in memory at full size on the way to being scaled down anyway.
 */
private fun sampleSizeFor(longestEdge: Int): Int {
    var sample = 1
    while (longestEdge / (sample * 2) >= MAX_EDGE) sample *= 2
    return sample
}

private fun Bitmap.rotated(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = android.graphics.Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.cappedTo(maxEdge: Int): Bitmap {
    val longestEdge = maxOf(width, height)
    if (longestEdge <= maxEdge) return this
    val ratio = maxEdge.toFloat() / longestEdge
    return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
}
