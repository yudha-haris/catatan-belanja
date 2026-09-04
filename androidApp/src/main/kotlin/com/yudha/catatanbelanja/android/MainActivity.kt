package com.yudha.catatanbelanja.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppDialogHost
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.locale.AppLocale
import com.yudha.catatanbelanja.android.navigation.AppNavHost
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.app.presentation.AppViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is light-theme only, so the bars must be too: `enableEdgeToEdge()` on its own
        // picks the icon colour from the *system* dark mode, which paints white icons onto our
        // white background and hides the clock and the battery entirely.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            val viewModel: AppViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.load()
            }

            AppLocale(language = state.language) {
                AppTheme(flavor = state.themeFlavor) {
                    AppDialogHost {
                        // Nothing routes until the boot read lands: the graph needs to know
                        // whether a session is still running before it decides where the app
                        // opens. A failed read still boots, on the defaults.
                        when (state.loadState) {
                            UiState.Initial, UiState.Loading -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppTheme.colors.background),
                            )

                            is UiState.Success, is UiState.Error -> AppNavHost(
                                openLiveSessionOnStart = state.hasActiveSession,
                            )
                        }
                    }
                }
            }
        }
    }
}
