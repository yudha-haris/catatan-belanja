package com.yudha.catatanbelanja.core.domain.service

interface FileSharer {
    /** Writes [content] to a shareable cache file named [fileName] and opens the system share sheet. */
    suspend fun shareText(fileName: String, mimeType: String, content: String)

    /**
     * The same for an already-encoded image. Separate from [shareText] because the share sheet
     * wants the bytes verbatim — a PNG round-tripped through a String is no longer a PNG.
     */
    suspend fun shareImage(fileName: String, mimeType: String, bytes: ByteArray)
}
