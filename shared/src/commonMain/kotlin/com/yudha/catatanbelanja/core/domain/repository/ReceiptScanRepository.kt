package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ReceiptScan

interface ReceiptScanRepository {
    /**
     * Reads [image] — a scaled, JPEG-encoded receipt photo — into a draft trip. Nothing is written
     * to the database here; the draft goes to the review screen and only what the user confirms
     * there is saved, through [SessionRepository.importFinishedSession].
     */
    suspend fun scan(image: ByteArray): Resource<ReceiptScan>

    /** False while no OpenRouter key has been compiled in, which hides the scan entry point. */
    fun isAvailable(): Boolean
}
