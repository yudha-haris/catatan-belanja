package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppPhotoFrame
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** The attached receipt photo at full size, with the only two things left to do to it. */
@Composable
fun ReceiptPhotoBottomSheet(
    title: String,
    photoPath: String,
    photoContentDescription: String,
    missingLabel: String,
    replaceText: String,
    removeText: String,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = title,
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        AppPhotoFrame(
            path = photoPath,
            contentDescription = photoContentDescription,
            missingLabel = missingLabel,
            modifier = Modifier.heightIn(max = VIEWER_MAX_HEIGHT),
            contentScale = ContentScale.Fit,
        )

        Spacer(Modifier.height(Spacing.x16))
        AppButton(
            text = replaceText,
            onClick = onReplace,
            variant = AppButtonVariant.Ghost,
        )
        Spacer(Modifier.height(Spacing.x8))
        AppButton(
            text = removeText,
            onClick = onRemove,
            variant = AppButtonVariant.Danger,
        )
    }
}

private val VIEWER_MAX_HEIGHT = 460.dp
