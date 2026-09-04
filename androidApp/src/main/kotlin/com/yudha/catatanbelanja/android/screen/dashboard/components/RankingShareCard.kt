package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppDonutChart
import com.yudha.catatanbelanja.android.designsystem.component.display.AppDonutSlice
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.dashboard.domain.model.ShareSlice

/**
 * How concentrated the spending is: one ring, five named arcs and a sixth for the tail.
 *
 * The ramp is one hue stepped down in opacity rather than six unrelated colours. Six hues would
 * read as six categories that mean something; these arcs only run "biggest" to "smallest", and the
 * ranking underneath is where the meaning lives.
 */
@Composable
internal fun RankingShareCard(
    slices: List<ShareSlice>,
    leaderLabel: String,
    leaderPercent: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val otherLabel = stringResource(R.string.ranking_other)
    val arcs = slices.map { slice ->
        AppDonutSlice(
            fraction = slice.fraction,
            color = shareColor(slice.colorIndex, colors.primary, colors.line),
        )
    }

    AppCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.ranking_share_title),
            style = AppTheme.typography.sectionTitle,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AppDonutChart(
            slices = arcs,
            centerValue = stringResource(R.string.common_percent, leaderPercent),
            centerLabel = stringResource(R.string.ranking_share_center_hint),
        )
        Spacer(Modifier.height(Spacing.x10))
        Text(
            // The tail can top the ranking when spending is spread thin; it has no name of its own.
            text = stringResource(
                R.string.ranking_share_leader,
                leaderLabel.ifBlank { otherLabel },
                leaderPercent,
            ),
            style = AppTheme.typography.muted,
            color = colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x14))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.x8)) {
            slices.forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.x10),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shareColor(slice.colorIndex, colors.primary, colors.line)),
                    )
                    Text(
                        text = when (slice.isOther) {
                            true -> otherLabel
                            false -> "${slice.emoji} ${slice.label}"
                        },
                        modifier = Modifier.weight(1f),
                        style = AppTheme.typography.body,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.common_percent, slice.percent),
                        style = AppTheme.typography.price,
                        color = colors.inkSecondary,
                    )
                }
            }
        }
    }
}

/** The ramp: the flavour's own hue stepped down, with the tail arc dropping out to the hairline. */
private fun shareColor(index: Int, primary: Color, line: Color): Color {
    val alpha = SHARE_ALPHAS.getOrNull(index) ?: return line
    return primary.copy(alpha = alpha)
}

private val SHARE_ALPHAS = listOf(1f, 0.78f, 0.58f, 0.42f, 0.30f)
