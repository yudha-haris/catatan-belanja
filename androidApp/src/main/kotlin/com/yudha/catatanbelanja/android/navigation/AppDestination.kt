package com.yudha.catatanbelanja.android.navigation

private const val LIVE_SESSION_PATH = "live"
private const val SESSION_DETAIL_PATH = "detail"
private const val COMPARE_PATH = "compare"

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
    }

    object Arg {
        const val REPEAT_FROM = "repeatFrom"
        const val SESSION_ID = "sessionId"
        const val A_ID = "aId"
        const val B_ID = "bId"
    }
}
