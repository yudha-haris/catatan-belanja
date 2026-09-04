package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.domain.service.FileSharer

/** Placeholder so the iOS target keeps compiling — there is no iOS UI to present a sheet from yet. */
class IosFileSharer : FileSharer {

    override suspend fun shareText(fileName: String, mimeType: String, content: String) {
        // TODO: write to NSTemporaryDirectory and present a UIActivityViewController.
    }

    override suspend fun shareImage(fileName: String, mimeType: String, bytes: ByteArray) {
        // TODO: the same, once there is an iOS receipt to render.
    }
}
