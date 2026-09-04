package com.yudha.catatanbelanja.features.dashboard.presentation

sealed interface DashboardEffect {
    /** The empty state's "Coba dengan data contoh" finished — the screen toasts and the tab fills. */
    data object DemoSeeded : DashboardEffect
}
