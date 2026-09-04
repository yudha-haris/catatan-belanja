package com.yudha.catatanbelanja.core.domain.service

/**
 * Somewhere to keep an image the user attached, outside the database and outside the cache the
 * system is free to sweep. Repositories own it; a ViewModel never sees one (§8).
 */
interface ImageStore {
    /**
     * Writes [bytes] under [name], replacing whatever was there, and returns the absolute path to
     * read it back with. [name] is a bare file name, not a path.
     */
    suspend fun save(name: String, bytes: ByteArray): String

    /** Removes the file at [path]. A path that is already gone is not an error. */
    suspend fun delete(path: String)
}
