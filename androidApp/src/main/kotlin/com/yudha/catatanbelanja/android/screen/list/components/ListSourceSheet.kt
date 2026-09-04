package com.yudha.catatanbelanja.android.screen.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.list.domain.model.ListSource

/**
 * "Buat daftar": every row here produces a finished list in one tap. Typing one out by hand is
 * offered first but is deliberately not the only way in — that is the habit the feature breaks.
 */
@Composable
internal fun ListSourceSheet(
    sources: List<ListSource>,
    onPick: (ListSource) -> Unit,
    onDeleteTemplate: (ListSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.list_source_sheet_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x16))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            sources.forEach { source ->
                val title = when (source.kind) {
                    ListSource.Kind.BLANK -> stringResource(R.string.list_source_blank)
                    ListSource.Kind.LAST_SESSION -> stringResource(R.string.list_source_last_session)
                    ListSource.Kind.LOW_STOCK -> stringResource(R.string.list_source_low_stock)
                    ListSource.Kind.TEMPLATE -> source.label.ifBlank {
                        stringResource(R.string.list_source_template)
                    }
                }
                val subtitle = when (source.kind) {
                    ListSource.Kind.BLANK -> stringResource(R.string.list_source_blank_subtitle)
                    ListSource.Kind.LAST_SESSION,
                    ListSource.Kind.LOW_STOCK,
                    ListSource.Kind.TEMPLATE,
                    -> pluralStringResource(
                        R.plurals.list_source_items,
                        source.names.size,
                        source.names.size,
                    )
                }
                val emoji = when (source.kind) {
                    ListSource.Kind.BLANK -> "✏️"
                    ListSource.Kind.LAST_SESSION -> "🔁"
                    ListSource.Kind.LOW_STOCK -> "📦"
                    ListSource.Kind.TEMPLATE -> "⭐"
                }

                val isTemplate = source.kind == ListSource.Kind.TEMPLATE
                ListSourceRow(
                    emoji = emoji,
                    title = title,
                    subtitle = subtitle,
                    onClick = { onPick(source) },
                    onDelete = when (isTemplate) {
                        true -> ({ onDeleteTemplate(source) })
                        false -> null
                    },
                    deleteContentDescription = when (isTemplate) {
                        true -> stringResource(R.string.list_template_delete_cd, title)
                        false -> null
                    },
                )
            }
        }
        Spacer(Modifier.height(Spacing.x8))
    }
}
