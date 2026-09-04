package com.yudha.catatanbelanja.core.domain.service

/**
 * Whether the device has a working connection right now. Consulted by the receipt scanner — the
 * only part of the app that needs one (`docs/architecture.md` §6b).
 *
 * Not a `Flow`: nothing in this app reacts to the network coming and going, it only asks at the
 * two moments it matters — before sending the user off to photograph something, and again before
 * the request itself, because a connection can drop in between.
 */
interface NetworkMonitor {
    fun isOnline(): Boolean
}
