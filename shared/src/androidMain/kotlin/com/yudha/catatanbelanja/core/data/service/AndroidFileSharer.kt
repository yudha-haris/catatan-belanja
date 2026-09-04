package com.yudha.catatanbelanja.core.data.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Writes the backup into `cacheDir/backups/` and hands it to the system share sheet through the
 * app's `FileProvider` — the manifest declares the matching `cache-path` for that folder.
 */
class AndroidFileSharer(private val context: Context) : FileSharer {

    override suspend fun shareText(fileName: String, mimeType: String, content: String) {
        val uri = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, BACKUP_DIRECTORY)
            directory.mkdirs()
            val file = File(directory, fileName)
            file.writeText(content)
            FileProvider.getUriForFile(context, "${context.packageName}$PROVIDER_SUFFIX", file)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
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
        const val PROVIDER_SUFFIX = ".fileprovider"
    }
}
