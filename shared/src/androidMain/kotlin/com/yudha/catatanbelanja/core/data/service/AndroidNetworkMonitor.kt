package com.yudha.catatanbelanja.core.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.yudha.catatanbelanja.core.domain.service.NetworkMonitor

/**
 * Asks `ConnectivityManager` for the active network's capabilities.
 *
 * `NET_CAPABILITY_VALIDATED` as well as `NET_CAPABILITY_INTERNET`, deliberately: the first says
 * the system actually reached the internet over this network, the second only that it is the kind
 * of network that normally can. Café wifi behind a login page has the second and not the first,
 * and is exactly the case where the scan would otherwise hang until it timed out.
 */
class AndroidNetworkMonitor(private val context: Context) : NetworkMonitor {

    override fun isOnline(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
