package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.stock.domain.model.RateConfidence

private const val DOT_COUNT = 3

/**
 * How sure the estimate is, as three dots that fill in. The estimate gets better the more the user
 * updates their stock, and a meter that visibly levels up is the cheapest way to say so — the
 * alternative is a sentence nobody reads twice.
 */
@Composable
internal fun StockConfidenceDots(
    confidence: RateConfidence,
    modifier: Modifier = Modifier,
) {
    val filled = when (confidence) {
        RateConfidence.LOW -> 1
        RateConfidence.MEDIUM -> 2
        RateConfidence.HIGH, RateConfidence.EXACT -> DOT_COUNT
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.x4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOT_COUNT) { index ->
            val color by animateColorAsState(
                targetValue = when {
                    index < filled -> AppTheme.colors.primary
                    else -> AppTheme.colors.line
                },
                label = "confidenceDot",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
