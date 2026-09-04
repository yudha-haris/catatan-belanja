package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.core.common.Failure

/**
 * App-wide feedback controller. Replaces the rulebook's `ScreenDialogs` mixin — there is no
 * Snackbar in this app.
 */
@Stable
class AppUiController {
    internal var toastMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Bumped on every [showToast], because the message alone is not enough to identify one.
     * Two identical toasts in a row are the same `String`, so a host keyed on the text would not
     * restart its timer — and if the second arrives in the same frame the first one's timeout
     * clears, the pill is left on screen with nothing left to take it down.
     */
    internal var toastId by mutableStateOf(0)
        private set
    internal var isLoading by mutableStateOf(false)
        private set
    internal var errorFailure by mutableStateOf<Failure?>(null)
        private set
    internal var successMessage by mutableStateOf<String?>(null)
        private set
    internal var celebrationId by mutableStateOf(0)
        private set
    internal var isCelebrating by mutableStateOf(false)
        private set

    fun showToast(message: String) {
        toastMessage = message
        toastId += 1
    }

    fun showLoading() {
        isLoading = true
    }

    fun dismissLoading() {
        isLoading = false
    }

    fun showError(failure: Failure) {
        isLoading = false
        errorFailure = failure
    }

    fun showSuccess(message: String) {
        isLoading = false
        successMessage = message
    }

    fun celebrate() {
        celebrationId += 1
        isCelebrating = true
    }

    internal fun clearToast() {
        toastMessage = null
    }

    internal fun dismissError() {
        errorFailure = null
    }

    internal fun dismissSuccess() {
        successMessage = null
    }

    internal fun endCelebration() {
        isCelebrating = false
    }
}

val LocalAppUi: ProvidableCompositionLocal<AppUiController> = staticCompositionLocalOf {
    error("LocalAppUi accessed outside AppDialogHost")
}

/** Installs the controller once, above every screen, and renders its overlays. */
@Composable
fun AppDialogHost(content: @Composable () -> Unit) {
    val controller = remember { AppUiController() }
    CompositionLocalProvider(LocalAppUi provides controller) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            val failure = controller.errorFailure
            if (failure != null) {
                ErrorDialog(failure = failure, onDismiss = controller::dismissError)
            }
            val success = controller.successMessage
            if (success != null) {
                SuccessDialog(message = success, onDismiss = controller::dismissSuccess)
            }
            if (controller.isLoading) {
                LoadingDialog()
            }
            key(controller.celebrationId) {
                SuccessBurst(
                    visible = controller.isCelebrating,
                    onFinished = controller::endCelebration,
                )
            }
            AppToastHost(
                message = controller.toastMessage,
                toastId = controller.toastId,
                onTimeout = controller::clearToast,
            )
        }
    }
}
