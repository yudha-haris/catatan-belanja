package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.domain.service.NetworkMonitor

/**
 * Placeholder so the iOS target keeps compiling — there is no iOS UI to scan a receipt from yet.
 * Answers "online" rather than "offline": a stub that blocked the one feature it stands in for
 * would look like a bug the day the iOS screen arrives.
 */
class IosNetworkMonitor : NetworkMonitor {
    override fun isOnline(): Boolean = true
}
