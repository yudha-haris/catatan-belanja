package com.yudha.catatanbelanja.android.screen.list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppLevelBar
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * How far through the plan the trip is. The count rolls rather than cuts, because watching
 * "3/7" become "4/7" is most of the reason ticking things off feels good.
 */
@Composable
internal fun ListProgressCard(
    checkedCount: Int,
    totalCount: Int,
    remainingCount: Int,
    progress: Float,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = checkedCount,
                    transitionSpec = {
                        val rising = targetState > initialState
                        val direction = if (rising) 1 else -1
                        (
                            slideInVertically { height -> direction * height } + fadeIn()
                            ) togetherWith (
                            slideOutVertically { height -> -direction * height } + fadeOut()
                            )
                    },
                    label = "listProgressCount",
                ) { count ->
                    Text(
                        text = stringResource(R.string.list_progress_count, count, totalCount),
                        style = AppTheme.typography.statValue,
                        color = colors.ink,
                    )
                }
                Text(
                    text = when (isComplete) {
                        true -> stringResource(R.string.list_progress_done)
                        false -> pluralStringResource(
                            R.plurals.list_progress_remaining,
                            remainingCount,
                            remainingCount,
                        )
                    },
                    style = AppTheme.typography.muted,
                )
            }

            Text(
                text = if (isComplete) "🎉" else "🛒",
                style = AppTheme.typography.emojiLarge,
            )
        }

        Spacer(Modifier.height(Spacing.x12))
        AppLevelBar(progress = progress)
    }
}
