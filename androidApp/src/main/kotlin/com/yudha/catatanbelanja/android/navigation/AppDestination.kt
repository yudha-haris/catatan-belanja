package com.yudha.catatanbelanja.android.navigation

import android.net.Uri

private const val LIVE_SESSION_PATH = "live"
private const val SESSION_DETAIL_PATH = "detail"
private const val COMPARE_PATH = "compare"
private const val PRICE_TREND_PATH = "report/trend"

/**
 * Every route in the app. Instances build the concrete [route] to navigate to; [Pattern] holds
 * the templates `AppNavHost` registers, so the two can never drift apart.
 */
sealed interface AppDestination {

    val route: String

    /** The four tabs: Belanja / Riwayat / Stok / Ringkasan. */
    data object Shell : AppDestination {
        override val route: String = Pattern.SHELL
    }

    data object Settings : AppDestination {
        override val route: String = Pattern.SETTINGS
    }

    /** Daftar belanja — the plan for the next trip. */
    data object ShoppingList : AppDestination {
        override val route: String = Pattern.SHOPPING_LIST
    }

    /**
     * [repeatFromSessionId] is the session the user tapped "belanja lagi" on — the live view
     * model reads its item names back to seed the "sering dibeli" chips.
     */
    data class LiveSession(val repeatFromSessionId: String? = null) : AppDestination {
        override val route: String = when (repeatFromSessionId) {
            null -> LIVE_SESSION_PATH
            else -> "$LIVE_SESSION_PATH?${Arg.REPEAT_FROM}=$repeatFromSessionId"
        }
    }

    data class SessionDetail(val sessionId: String) : AppDestination {
        override val route: String = "$SESSION_DETAIL_PATH/$sessionId"
    }

    /** The full spending history behind the summary tab's "8 belanja terakhir" card. */
    data object SpendingReport : AppDestination {
        override val route: String = Pattern.SPENDING_REPORT
    }

    /** The full ranking behind the summary tab's "Pengeluaran terbesar" card. */
    data object SpendingRanking : AppDestination {
        override val route: String = Pattern.SPENDING_RANKING
    }

    /**
     * One item's price trend, plus the manual adjustments behind it. [name] is the item to open
     * on; null lets the screen fall back to whatever was bought most, which is what the summary
     * card's own picker does.
     */
    data class PriceTrend(val name: String? = null) : AppDestination {
        override val route: String = when (name) {
            null -> PRICE_TREND_PATH
            else -> "$PRICE_TREND_PATH?${Arg.TREND_NAME}=${name.encodedForRoute()}"
        }
    }

    /** [aId] is always the older session, [bId] the newer one. */
    data class Compare(val aId: String, val bId: String) : AppDestination {
        override val route: String = "$COMPARE_PATH/$aId/$bId"
    }

    object Pattern {
        const val SHELL = "shell"
        const val SETTINGS = "settings"
        const val SHOPPING_LIST = "list"
        const val LIVE_SESSION = "$LIVE_SESSION_PATH?${Arg.REPEAT_FROM}={${Arg.REPEAT_FROM}}"
        const val SESSION_DETAIL = "$SESSION_DETAIL_PATH/{${Arg.SESSION_ID}}"
        const val COMPARE = "$COMPARE_PATH/{${Arg.A_ID}}/{${Arg.B_ID}}"
        const val SPENDING_REPORT = "report/spending"
        const val SPENDING_RANKING = "report/ranking"
        const val PRICE_TREND = "$PRICE_TREND_PATH?${Arg.TREND_NAME}={${Arg.TREND_NAME}}"
    }

    object Arg {
        const val REPEAT_FROM = "repeatFrom"
        const val SESSION_ID = "sessionId"
        const val A_ID = "aId"
        const val B_ID = "bId"
        const val TREND_NAME = "trendName"
    }
}

/**
 * Item names are user data — "Minyak goreng / 2L" would end the route early at the slash, and a
 * space would not survive the URI at all. `Uri.encode` rather than `URLEncoder`, which spells a
 * space `+`: navigation decodes arguments with `Uri.decode`, and that would hand the screen a
 * name with a literal plus in it. The other routes carry generated ids, which is why this
 * is the only one that needs it.
 */
private fun String.encodedForRoute(): String = Uri.encode(this)
