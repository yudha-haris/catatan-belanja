package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * The rubber stamp a cashier thumps onto a paid receipt: tracked caps in a double outline, sitting
 * a few degrees off square. Deliberately a little faded — a stamp that lined up perfectly and
 * printed at full strength would read as another button.
 */
@Composable
fun AppStampBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    rotationDegrees: Float = STAMP_ROTATION,
) {
    val shape = RoundedCornerShape(AppTheme.shapes.radiusSmall)
    val ink = color.copy(alpha = STAMP_ALPHA)

    Text(
        text = text,
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees }
            .border(OUTER_STROKE, ink, shape)
            .padding(OUTER_GAP)
            .border(INNER_STROKE, ink, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = AppTheme.typography.receiptStamp,
        color = ink,
        maxLines = 1,
    )
}

private const val STAMP_ROTATION = -9f
private const val STAMP_ALPHA = 0.62f
private val OUTER_STROKE = 2.dp
private val INNER_STROKE = 1.dp
private val OUTER_GAP = 3.dp
