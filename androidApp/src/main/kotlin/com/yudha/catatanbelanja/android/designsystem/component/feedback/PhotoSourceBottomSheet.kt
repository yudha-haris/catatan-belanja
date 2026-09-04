package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val CAMERA_EMOJI = "📷"
private const val GALLERY_EMOJI = "🖼"

/**
 * Where a picture comes from: the camera, or one already on the phone.
 *
 * [canUseCamera] false drops the camera option rather than disabling it — a device with no camera
 * is not a user who did something wrong, and a greyed-out button here explains nothing.
 */
@Composable
fun PhotoSourceBottomSheet(
    title: String,
    message: String,
    cameraText: String,
    galleryText: String,
    cancelText: String,
    canUseCamera: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = title,
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x6))
        Text(text = message, style = AppTheme.typography.body)
        Spacer(Modifier.height(Spacing.x20))

        if (canUseCamera) {
            AppButton(
                text = cameraText,
                onClick = onCamera,
                emoji = CAMERA_EMOJI,
                big = true,
            )
            Spacer(Modifier.height(Spacing.x8))
        }

        AppButton(
            text = galleryText,
            onClick = onGallery,
            variant = AppButtonVariant.Ghost,
            emoji = GALLERY_EMOJI,
            big = canUseCamera.not(),
        )
        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = cancelText,
            onClick = onDismiss,
            variant = AppButtonVariant.Soft,
        )
    }
}
