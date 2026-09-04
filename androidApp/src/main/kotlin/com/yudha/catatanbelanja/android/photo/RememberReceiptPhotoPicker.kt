package com.yudha.catatanbelanja.android.photo

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Wires the camera and the photo picker up to [onPhoto], which receives the picture already scaled
 * and encoded — see [readReceiptPhoto].
 *
 * Picking a photo is an Android UI concern, the way reading the import file is (§6): the screen
 * runs the contracts and hands the ViewModel the bytes it got, never a `Uri` the ViewModel would
 * have to resolve. [onFailed] covers a picture the decoder will not read — a corrupt file, or a
 * document provider that has already withdrawn the grant.
 */
@Composable
fun rememberReceiptPhotoPicker(
    onPhoto: (ByteArray) -> Unit,
    onFailed: () -> Unit,
): ReceiptPhotoPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // The file the camera app is told to write into. Local UI plumbing, held only for as long as
    // that app is in the foreground.
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = readReceiptPhoto(context, uri)
            if (bytes == null) onFailed() else onPhoto(bytes)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { saved ->
        val uri = captureUri
        captureUri = null
        if (!saved || uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = readReceiptPhoto(context, uri)
            // The full-size original has been scaled down by now, so the staging file in the cache
            // has done its job either way.
            withContext(Dispatchers.IO) { context.contentResolver.delete(uri, null, null) }
            if (bytes == null) onFailed() else onPhoto(bytes)
        }
    }

    val hasCamera = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    return remember(hasCamera, galleryLauncher, cameraLauncher) {
        ReceiptPhotoPicker(
            canTakePhoto = hasCamera,
            onTakePhoto = {
                val uri = newCaptureUri(context)
                captureUri = uri
                if (uri == null) onFailed() else cameraLauncher.launch(uri)
            },
            onPickFromGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        )
    }
}

/**
 * A fresh, empty file in the cache for the camera app to fill, exposed through the app's
 * `FileProvider` — the manifest declares the matching `captures` cache-path.
 */
private fun newCaptureUri(context: android.content.Context): Uri? = runCatching {
    val directory = File(context.cacheDir, CAPTURE_DIRECTORY)
    directory.mkdirs()
    val file = File(directory, CAPTURE_PREFIX + System.currentTimeMillis() + CAPTURE_SUFFIX)
    file.createNewFile()
    FileProvider.getUriForFile(context, context.packageName + PROVIDER_SUFFIX, file)
}.getOrNull()

private const val CAPTURE_DIRECTORY = "captures"
private const val CAPTURE_PREFIX = "struk-"
private const val CAPTURE_SUFFIX = ".jpg"
private const val PROVIDER_SUFFIX = ".fileprovider"
