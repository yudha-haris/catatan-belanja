package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

private const val CAMERA_EMOJI = "📸"

/**
 * The receipt photo, in the one slot it occupies on both the live session and the finished trip.
 *
 * With no photo it is an offer, not a demand: a flat card, one quiet button, and no red anything —
 * most trips will never have a picture attached and that has to look completely fine.
 */
@Composable
fun ReceiptPhotoCard(
    title: String,
    hint: String,
    photoPath: String?,
    addActionText: String,
    photoContentDescription: String,
    missingLabel: String,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier, flat = photoPath == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = CAMERA_EMOJI, style = AppTheme.typography.emoji)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.sectionTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(Spacing.x4))
        Text(
            text = hint,
            style = AppTheme.typography.muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.x12))

        if (photoPath == null) {
            AppButton(
                text = addActionText,
                onClick = onAdd,
                variant = AppButtonVariant.Ghost,
                emoji = CAMERA_EMOJI,
            )
            return@AppCard
        }

        AppPhotoFrame(
            path = photoPath,
            contentDescription = photoContentDescription,
            missingLabel = missingLabel,
            modifier = Modifier.height(THUMBNAIL_HEIGHT),
            onClick = onOpen,
        )
    }
}

private val THUMBNAIL_HEIGHT = 172.dp
