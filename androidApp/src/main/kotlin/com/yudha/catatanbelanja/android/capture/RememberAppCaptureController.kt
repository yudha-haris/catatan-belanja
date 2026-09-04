package com.yudha.catatanbelanja.android.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** The controller to pair with one [AppCaptureBox]. */
@Composable
fun rememberAppCaptureController(): AppCaptureController = remember { AppCaptureController() }
