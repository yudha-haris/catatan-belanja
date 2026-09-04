package com.yudha.catatanbelanja.core.data.service

import android.content.Context
import com.yudha.catatanbelanja.core.domain.service.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Receipt photos live in `filesDir/receipts/`, not in the cache: the cache is the system's to
 * sweep whenever storage runs short, and a receipt that quietly disappeared would be worse than
 * one that was never taken. They stay out of the cloud backup for the same reason the database
 * rules them out — see `res/xml/backup_rules.xml`.
 */
class AndroidImageStore(private val context: Context) : ImageStore {

    override suspend fun save(name: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, IMAGE_DIRECTORY)
        directory.mkdirs()
        val file = File(directory, name)
        // Written beside the target and moved into place, so a write that dies half way through
        // leaves the previous photo intact instead of a truncated JPEG the decoder returns as null.
        val staging = File(directory, name + STAGING_SUFFIX)
        staging.writeBytes(bytes)
        file.delete()
        staging.renameTo(file)
        file.absolutePath
    }

    override suspend fun delete(path: String) {
        withContext(Dispatchers.IO) { File(path).delete() }
    }

    private companion object {
        const val IMAGE_DIRECTORY = "receipts"
        const val STAGING_SUFFIX = ".tmp"
    }
}
