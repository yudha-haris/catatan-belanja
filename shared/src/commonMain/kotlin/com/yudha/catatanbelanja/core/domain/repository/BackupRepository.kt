package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ImportSummary

interface BackupRepository {
    /** The full backup document, pretty-printed. Compatible with the prototype's "Salin data". */
    suspend fun buildBackupJson(): Resource<String>

    /** Writes the backup to a cache file and opens the system share sheet. */
    suspend fun shareBackup(): Resource<Unit>

    suspend fun copyBackupToClipboard(): Resource<Unit>

    suspend fun importFromJson(rawJson: String): Resource<ImportSummary>

    suspend fun clearAllData(): Resource<Unit>

    suspend fun seedDemoData(): Resource<Unit>
}
