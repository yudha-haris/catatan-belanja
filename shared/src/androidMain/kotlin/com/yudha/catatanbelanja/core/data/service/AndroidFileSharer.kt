package com.yudha.catatanbelanja.core.data.service

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Writes what is being shared into `cacheDir` and hands it to the system share sheet through the
 * app's `FileProvider` — the manifest declares the matching `cache-path` for each folder. The cache
 * is the right home here: once the receiving app has copied the bytes, the file has done its job.
 */
class AndroidFileSharer(private val context: Context) : FileSharer {

    override suspend fun shareText(fileName: String, mimeType: String, content: String) {
        share(BACKUP_DIRECTORY, fileName, mimeType) { file -> file.writeText(content) }
    }

    override suspend fun shareImage(fileName: String, mimeType: String, bytes: ByteArray) {
        share(IMAGE_DIRECTORY, fileName, mimeType) { file -> file.writeBytes(bytes) }
    }

    private suspend fun share(
        directoryName: String,
        fileName: String,
        mimeType: String,
        write: (File) -> Unit,
    ) {
        val uri = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, directoryName)
            directory.mkdirs()
            val file = File(directory, fileName)
            write(file)
            FileProvider.getUriForFile(context, context.packageName + PROVIDER_SUFFIX, file)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            // The chooser draws a thumbnail of what is about to be sent only when the payload is
            // on the ClipData too, which is most of the point of sharing a picture of a receipt.
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, fileName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    private companion object {
        const val BACKUP_DIRECTORY = "backups"
        const val IMAGE_DIRECTORY = "shares"
        const val PROVIDER_SUFFIX = ".fileprovider"
    }
}
