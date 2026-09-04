package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/** Centred paper dialog: optional emoji, title, message, secondary detail and an actions slot. */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    message: String? = null,
    detail: String? = null,
    dismissible: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val colors = AppTheme.colors
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
        ),
    ) {
        Column(
            modifier = modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.shapes.radius))
                .background(colors.paper)
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (emoji != null) {
                Text(text = emoji, style = AppTheme.typography.emojiLarge)
                Spacer(Modifier.height(Spacing.x10))
            }
            Text(
                text = title,
                style = AppTheme.typography.sheetTitle,
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(Modifier.height(Spacing.x8))
                Text(
                    text = message,
                    style = AppTheme.typography.body,
                    color = colors.inkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            if (detail != null) {
                Spacer(Modifier.height(Spacing.x6))
                Text(
                    text = detail,
                    style = AppTheme.typography.tiny,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(Spacing.x18))
            actions()
        }
    }
}
