package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.domain.service.ImageStore

/** Placeholder so the iOS target keeps compiling — there is no iOS UI to attach a photo from yet. */
class IosImageStore : ImageStore {

    override suspend fun save(name: String, bytes: ByteArray): String = name

    override suspend fun delete(path: String) {
        // TODO: write into NSDocumentDirectory and remove through NSFileManager.
    }
}
