package com.yudha.catatanbelanja.core.domain.service

interface FileSharer {
    /** Writes [content] to a shareable cache file named [fileName] and opens the system share sheet. */
    suspend fun shareText(fileName: String, mimeType: String, content: String)
}
