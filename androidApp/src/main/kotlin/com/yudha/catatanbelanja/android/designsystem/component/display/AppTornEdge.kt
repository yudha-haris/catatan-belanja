package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * The torn-off edge of a paper receipt: a run of triangles in [color] on nothing, so whatever the
 * sheet is lying on shows through the gaps. Same 16dp pitch as `ReceiptHeader`'s zigzag.
 *
 * [pointingDown] is the bottom of a sheet — paper above, teeth hanging below. False is the top.
 */
@Composable
fun AppTornEdge(
    color: Color,
    modifier: Modifier = Modifier,
    pointingDown: Boolean = true,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(TOOTH_HEIGHT),
    ) {
        val pitch = TOOTH_PITCH.toPx()
        val base = if (pointingDown) 0f else size.height
        val tip = if (pointingDown) size.height else 0f
        val path = Path()
        path.moveTo(0f, base)
        var x = 0f
        while (x < size.width) {
            path.lineTo(x + pitch / 2f, tip)
            path.lineTo(x + pitch, base)
            x += pitch
        }
        path.lineTo(size.width, base)
        path.close()
        drawPath(path = path, color = color)
    }
}

private val TOOTH_PITCH = 16.dp
private val TOOTH_HEIGHT = 9.dp
